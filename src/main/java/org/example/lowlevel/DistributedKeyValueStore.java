package org.example.lowlevel;

import java.util.*;
import java.util.concurrent.*;

public class DistributedKeyValueStore {

    // --- 1. Versioned Value for Conflict Resolution (Last-Write-Wins) ---
    public static class VersionedValue {
        private final String value;
        private final long timestamp;

        public VersionedValue(String value, long timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public String getValue() { return value; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "Value{" + value + ", ts=" + timestamp + "}";
        }
    }

    // --- 2. Storage Node ---
    public static class DataNode {
        private final String nodeId;
        private final Map<String, VersionedValue> store = new ConcurrentHashMap<>();
        private volatile boolean active = true;

        public DataNode(String nodeId) {
            this.nodeId = nodeId;
        }

        public String getNodeId() { return nodeId; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public void put(String key, VersionedValue vv) {
            if (!active) throw new RuntimeException("Node [" + nodeId + "] is down!");
            store.compute(key, (k, existing) -> {
                if (existing == null || vv.getTimestamp() >= existing.getTimestamp()) {
                    return vv;
                }
                return existing; // Keep existing if newer (LWW)
            });
        }

        public VersionedValue get(String key) {
            if (!active) throw new RuntimeException("Node [" + nodeId + "] is down!");
            return store.get(key);
        }

        public Map<String, VersionedValue> getAllData() {
            return new HashMap<>(store);
        }
    }

    // --- 3. Cluster Topology & Partition Router ---
    public static class ClusterTopology {
        private final List<DataNode> nodes;
        private final int replicationFactor;

        public ClusterTopology(List<DataNode> nodes, int replicationFactor) {
            this.nodes = nodes;
            this.replicationFactor = Math.min(replicationFactor, nodes.size());
        }

        // Determine primary and replica nodes for a given key using consistent hashing / modulo hash
        public List<DataNode> getNodesForKey(String key) {
            List<DataNode> assignedNodes = new ArrayList<>();
            int hash = Math.abs(key.hashCode());
            int startIndex = hash % nodes.size();

            for (int i = 0; i < replicationFactor; i++) {
                int index = (startIndex + i) % nodes.size();
                assignedNodes.add(nodes.get(index));
            }
            return assignedNodes;
        }

        public List<DataNode> getAllNodes() {
            return nodes;
        }
    }

    // --- 4. Coordinator / Client Gateway (Quorum Operations) ---
    public static class Coordinator {
        private final ClusterTopology topology;

        public Coordinator(ClusterTopology topology) {
            this.topology = topology;
        }

        public void put(String key, String value, int writeQuorum) {
            List<DataNode> targetNodes = topology.getNodesForKey(key);
            long timestamp = System.currentTimeMillis();
            VersionedValue vv = new VersionedValue(value, timestamp);

            int successfulWrites = 0;
            for (DataNode node : targetNodes) {
                try {
                    if (node.isActive()) {
                        node.put(key, vv);
                        successfulWrites++;
                        System.out.println("  [Coordinator] PUT success on node [" + node.getNodeId() + "] for key [" + key + "]");
                    }
                } catch (Exception e) {
                    System.err.println("  [Coordinator] PUT failed on node [" + node.getNodeId() + "]: " + e.getMessage());
                }
            }

            if (successfulWrites < writeQuorum) {
                throw new RuntimeException("Write failed! Achieved quorum " + successfulWrites + "/" + writeQuorum);
            }
            System.out.println("[Coordinator] PUT completed for key [" + key + "] = " + value + " with quorum " + successfulWrites + "\n");
        }

        public String get(String key, int readQuorum) {
            List<DataNode> targetNodes = topology.getNodesForKey(key);
            List<VersionedValue> fetchedValues = new ArrayList<>();

            int successfulReads = 0;
            for (DataNode node : targetNodes) {
                try {
                    if (node.isActive()) {
                        VersionedValue vv = node.get(key);
                        if (vv != null) {
                            fetchedValues.add(vv);
                        }
                        successfulReads++;
                        System.out.println("  [Coordinator] GET read from node [" + node.getNodeId() + "]");
                    }
                } catch (Exception e) {
                    System.err.println("  [Coordinator] GET failed on node [" + node.getNodeId() + "]: " + e.getMessage());
                }
            }

            if (successfulReads < readQuorum) {
                throw new RuntimeException("Read failed! Achieved quorum " + successfulReads + "/" + readQuorum);
            }

            if (fetchedValues.isEmpty()) {
                return null;
            }

            // Resolve conflicts using Last-Write-Wins (highest timestamp)
            VersionedValue latest = fetchedValues.stream()
                    .max(Comparator.comparingLong(VersionedValue::getTimestamp))
                    .orElse(null);

            String result = latest != null ? latest.getValue() : null;
            System.out.println("[Coordinator] GET resolved key [" + key + "] -> " + result + " (from " + successfulReads + " responding nodes)\n");
            return result;
        }
    }

    // --- 5. Main Method Demonstrating Cluster Operations & Failover ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("   Starting Distributed Key-Value Store Cluster");
        System.out.println("==================================================\n");

        // Initialize 3 Data Nodes
        DataNode node1 = new DataNode("Node-1");
        DataNode node2 = new DataNode("Node-2");
        DataNode node3 = new DataNode("Node-3");

        List<DataNode> allNodes = Arrays.asList(node1, node2, node3);

        // Setup topology with a replication factor of 2
        ClusterTopology topology = new ClusterTopology(allNodes, 2);
        Coordinator coordinator = new Coordinator(topology);

        // 1. Perform Write Operations (Write Quorum = 2)
        System.out.println("--- Submitting Write Requests (Quorum = 2) ---");
        coordinator.put("user:101", "Alice", 2);
        coordinator.put("user:102", "Bob", 2);
        coordinator.put("user:103", "Charlie", 2);

        // 2. Perform Read Operations (Read Quorum = 1)
        System.out.println("--- Submitting Read Requests (Quorum = 1) ---");
        coordinator.get("user:101", 1);
        coordinator.get("user:102", 1);

        // 3. Simulate Node Failure and Quorum Resilience
        System.out.println("*** SIMULATING CRASH OF Node-1 ***");
        node1.setActive(false);
        System.out.println();

        System.out.println("--- Writing new data with Node-1 down (Write Quorum = 1) ---");
        coordinator.put("user:104", "Diana", 1);

        System.out.println("--- Reading data affected by Node-1 failure ---");
        // user:101 was replicated on Node-1 and Node-2. Since Node-2 is active, read quorum succeeds.
        coordinator.get("user:101", 1);
        coordinator.get("user:104", 1);

        // 4. Node Recovery
        System.out.println("*** RECOVERING Node-1 ***");
        node1.setActive(true);
        Thread.sleep(100);

        System.out.println("\n--- Checking Node-1 storage state after recovery ---");
        System.out.println("Node-1 data keys present: " + node1.getAllData().keySet());

        System.out.println("\n==================================================");
        System.out.println("   Distributed Key-Value Store Demo Completed");
        System.out.println("==================================================");
    }
}
