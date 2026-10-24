package org.example.lowlevel;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DistributedCacheCluster {

    // --- 1. Thread-Safe Local LRU Cache (Per Node) ---
    public static class LocalLRUCache<K, V> {
        private final int capacity;
        private final Map<K, Node<K, V>> map;
        private final Node<K, V> head;
        private final Node<K, V> tail;
        private final Lock lock = new ReentrantLock();

        private static class Node<K, V> {
            K key;
            V value;
            Node<K, V> prev;
            Node<K, V> next;

            Node(K key, V value) {
                this.key = key;
                this.value = value;
            }
        }

        public LocalLRUCache(int capacity) {
            this.capacity = capacity;
            this.map = new HashMap<>();
            head = new Node<>(null, null);
            tail = new Node<>(null, null);
            head.next = tail;
            tail.prev = head;
        }

        public V get(K key) {
            lock.lock();
            try {
                Node<K, V> node = map.get(key);
                if (node == null) return null;
                moveToHead(node);
                return node.value;
            } finally {
                lock.unlock();
            }
        }

        public void put(K key, V value) {
            lock.lock();
            try {
                Node<K, V> node = map.get(key);
                if (node != null) {
                    node.value = value;
                    moveToHead(node);
                } else {
                    if (map.size() >= capacity) {
                        Node<K, V> lru = removeTail();
                        if (lru != null) {
                            map.remove(lru.key);
                        }
                    }
                    Node<K, V> newNode = new Node<>(key, value);
                    addToHead(newNode);
                    map.put(key, newNode);
                }
            } finally {
                lock.unlock();
            }
        }

        public int size() {
            lock.lock();
            try {
                return map.size();
            } finally {
                lock.unlock();
            }
        }

        private void removeNode(Node<K, V> node) {
            node.prev.next = node.next;
            node.next.prev = node.prev;
        }

        private void addToHead(Node<K, V> node) {
            node.prev = head;
            node.next = head.next;
            head.next.prev = node;
            head.next = node;
        }

        private void moveToHead(Node<K, V> node) {
            removeNode(node);
            addToHead(node);
        }

        private Node<K, V> removeTail() {
            Node<K, V> lru = tail.prev;
            if (lru == head) return null;
            removeNode(lru);
            return lru;
        }
    }

    // --- 2. Storage Node Definition ---
    public static class CacheNode {
        private final String nodeId;
        private final LocalLRUCache<String, String> cache;
        private volatile boolean active = true;

        public CacheNode(String nodeId, int localCapacity) {
            this.nodeId = nodeId;
            this.cache = new LocalLRUCache<>(localCapacity);
        }

        public String getNodeId() { return nodeId; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public void put(String key, String value) {
            if (!active) throw new RuntimeException("Node [" + nodeId + "] is down!");
            cache.put(key, value);
        }

        public String get(String key) {
            if (!active) throw new RuntimeException("Node [" + nodeId + "] is down!");
            return cache.get(key);
        }

        public int size() {
            return cache.size();
        }
    }

    // --- 3. Consistent Hash Ring with Virtual Nodes ---
    public static class ConsistentHashRing {
        private final SortedMap<Integer, CacheNode> ring = new TreeMap<>();
        private final int virtualNodesPerNode;

        public ConsistentHashRing(int virtualNodesPerNode, List<CacheNode> nodes) {
            this.virtualNodesPerNode = virtualNodesPerNode;
            for (CacheNode node : nodes) {
                addNode(node);
            }
        }

        public void addNode(CacheNode node) {
            for (int i = 0; i < virtualNodesPerNode; i++) {
                int hash = hash(node.getNodeId() + "-vnode-" + i);
                ring.put(hash, node);
            }
        }

        public void removeNode(CacheNode node) {
            for (int i = 0; i < virtualNodesPerNode; i++) {
                int hash = hash(node.getNodeId() + "-vnode-" + i);
                ring.remove(hash);
            }
        }

        // Returns primary and backup nodes for replication
        public List<CacheNode> getNodesForKey(String key, int count) {
            List<CacheNode> result = new ArrayList<>();
            if (ring.isEmpty()) return result;

            int hash = hash(key);
            SortedMap<Integer, CacheNode> tailMap = ring.tailMap(hash);
            Iterator<Integer> iter = (tailMap.isEmpty() ? ring.keySet() : tailMap.keySet()).iterator();

            Set<CacheNode> visited = new HashSet<>();
            while (result.size() < count && (!tailMap.isEmpty() || !ring.isEmpty())) {
                if (!iter.hasNext()) {
                    iter = ring.keySet().iterator();
                }
                int h = iter.next();
                CacheNode node = ring.get(h);
                if (visited.add(node)) {
                    result.add(node);
                }
                if (visited.size() == ring.size()) break; // Prevent infinite loop if cluster is small
            }
            return result;
        }

        private int hash(String key) {
            // Standard Murmur/HashCode simulation
            int h = key.hashCode();
            h ^= (h >>> 20) ^ (h >>> 12);
            return h ^ (h >>> 7) ^ (h >>> 4);
        }
    }

    // --- 4. Distributed Cache Coordinator / Gateway ---
    public static class DistributedCacheClusterGateway {
        private final ConsistentHashRing hashRing;
        private final int replicationFactor;

        public DistributedCacheClusterGateway(ConsistentHashRing hashRing, int replicationFactor) {
            this.hashRing = hashRing;
            this.replicationFactor = replicationFactor;
        }

        public void put(String key, String value) {
            List<CacheNode> targetNodes = hashRing.getNodesForKey(key, replicationFactor);
            boolean success = false;

            for (CacheNode node : targetNodes) {
                try {
                    if (node.isActive()) {
                        node.put(key, value);
                        success = true;
                        System.out.println("  [Gateway] PUT success on node [" + node.getNodeId() + "] for key [" + key + "]");
                    }
                } catch (Exception e) {
                    System.err.println("  [Gateway] PUT failed on node [" + node.getNodeId() + "]: " + e.getMessage());
                }
            }

            if (!success) {
                throw new RuntimeException("Write failed across all replicas for key [" + key + "]");
            }
            System.out.println("[Gateway] Successfully completed PUT(" + key + " = " + value + ")\n");
        }

        public String get(String key) {
            List<CacheNode> targetNodes = hashRing.getNodesForKey(key, replicationFactor);

            for (CacheNode node : targetNodes) {
                try {
                    if (node.isActive()) {
                        String value = node.get(key);
                        if (value != null) {
                            System.out.println("[Gateway] Cache HIT for key [" + key + "] on node [" + node.getNodeId() + "]");
                            return value;
                        }
                    }
                } catch (Exception e) {
                    System.err.println("  [Gateway] GET failed on node [" + node.getNodeId() + "]: " + e.getMessage());
                }
            }

            System.out.println("[Gateway] Cache MISS for key [" + key + "]");
            return null;
        }
    }

    // --- 5. Main Method Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("   Starting Distributed Cache Cluster Simulation");
        System.out.println("==================================================\n");

        // Initialize 3 Cache Nodes with a small local capacity of 2 items each to test LRU eviction
        CacheNode nodeA = new CacheNode("Cache-Node-A", 2);
        CacheNode nodeB = new CacheNode("Cache-Node-B", 2);
        CacheNode nodeC = new CacheNode("Cache-Node-C", 2);

        List<CacheNode> initialNodes = Arrays.asList(nodeA, nodeB, nodeC);

        // Build Consistent Hash Ring with 3 virtual nodes per physical node
        ConsistentHashRing hashRing = new ConsistentHashRing(3, initialNodes);

        // Setup gateway with replication factor of 2 (primary + 1 replica)
        DistributedCacheClusterGateway gateway = new DistributedCacheClusterGateway(hashRing, 2);

        // 1. Populate Cache Entries
        System.out.println("--- Phase 1: Inserting Cache Items ---");
        gateway.put("session:1001", "TokenDataA");
        gateway.put("session:1002", "TokenDataB");
        gateway.put("session:1003", "TokenDataC");
        gateway.put("session:1004", "TokenDataD");

        // 2. Read Back Entries
        System.out.println("--- Phase 2: Fetching Cache Items ---");
        gateway.get("session:1001");
        gateway.get("session:1003");

        // 3. Simulate Node Failure and Failover Read Resilience
        System.out.println("\n*** SIMULATING FAILURE OF Cache-Node-A ***");
        nodeA.setActive(false);

        System.out.println("--- Phase 3: Reading data when a node is down (leveraging replicas) ---");
        // Because replication factor is 2, items stored on Node A are also backed up on another node.
        gateway.get("session:1001");

        // 4. Node Recovery & Rejoin
        System.out.println("\n*** RECOVERING Cache-Node-A ***");
        nodeA.setActive(true);
        Thread.sleep(50);

        System.out.println("--- Phase 4: Post-recovery Verification ---");
        gateway.get("session:1001");

        System.out.println("\n==================================================");
        System.out.println("   Distributed Cache Cluster Demo Completed");
        System.out.println("==================================================");
    }
}
