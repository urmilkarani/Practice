package org.example.lowlevel;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class DistributedMessageQueue {

    // --- 1. Message Representation ---
    public static class Message {
        private final long offset;
        private final String key;
        private final String payload;
        private final long timestamp;

        public Message(long offset, String key, String payload) {
            this.offset = offset;
            this.key = key;
            this.payload = payload;
            this.timestamp = System.currentTimeMillis();
        }

        public long getOffset() { return offset; }
        public String getKey() { return key; }
        public String getPayload() { return payload; }
        public long getTimestamp() { return timestamp; }

        @Override
        public String toString() {
            return "Msg{offset=" + offset + ", key='" + key + "', payload='" + payload + "'}";
        }
    }

    // --- 2. Partition Append-Only Log ---
    public static class PartitionLog {
        private final String topic;
        private final int partitionId;
        private final List<Message> log = new CopyOnWriteArrayList<>();
        private final AtomicLong offsetCounter = new AtomicLong(0);

        public PartitionLog(String topic, int partitionId) {
            this.topic = topic;
            this.partitionId = partitionId;
        }

        public synchronized Message append(String key, String payload) {
            long offset = offsetCounter.getAndIncrement();
            Message msg = new Message(offset, key, payload);
            log.add(msg);
            return msg;
        }

        public List<Message> fetch(long startOffset, int maxRecords) {
            List<Message> result = new ArrayList<>();
            for (Message msg : log) {
                if (msg.getOffset() >= startOffset) {
                    result.add(msg);
                    if (result.size() >= maxRecords) {
                        break;
                    }
                }
            }
            return result;
        }

        public int getPartitionId() { return partitionId; }
        public long getHighWatermark() { return offsetCounter.get(); }
    }

    // --- 3. Broker Node ---
    public static class BrokerNode {
        private final String brokerId;
        // Key: topic-partitionId, Value: PartitionLog
        private final Map<String, PartitionLog> partitionLogs = new ConcurrentHashMap<>();
        private volatile boolean active = true;

        public BrokerNode(String brokerId) {
            this.brokerId = brokerId;
        }

        public String getBrokerId() { return brokerId; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public void createPartition(String topic, int partitionId) {
            String key = topic + "-" + partitionId;
            partitionLogs.putIfAbsent(key, new PartitionLog(topic, partitionId));
        }

        public PartitionLog getPartition(String topic, int partitionId) {
            if (!active) throw new RuntimeException("Broker [" + brokerId + "] is down!");
            return partitionLogs.get(topic + "-" + partitionId);
        }
    }

    // --- 4. Cluster Metadata & Router ---
    public static class ClusterManager {
        private final Map<String, BrokerNode> brokers = new ConcurrentHashMap<>();
        // Maps topic -> list of partition leader broker IDs
        private final Map<String, List<String>> topicPartitions = new ConcurrentHashMap<>();

        public void registerBroker(BrokerNode broker) {
            brokers.put(broker.getBrokerId(), broker);
        }

        public void createTopic(String topic, int numPartitions, List<String> brokerIds) {
            List<String> partitionLeaders = new ArrayList<>();
            for (int i = 0; i < numPartitions; i++) {
                String targetBrokerId = brokerIds.get(i % brokerIds.size());
                BrokerNode broker = brokers.get(targetBrokerId);
                if (broker != null) {
                    broker.createPartition(topic, i);
                    partitionLeaders.add(targetBrokerId);
                }
            }
            topicPartitions.put(topic, partitionLeaders);
        }

        public BrokerNode getLeaderForPartition(String topic, int partitionId) {
            List<String> leaders = topicPartitions.get(topic);
            if (leaders == null || partitionId >= leaders.size()) {
                throw new RuntimeException("Invalid topic or partition ID!");
            }
            String brokerId = leaders.get(partitionId);
            return brokers.get(brokerId);
        }

        public int getPartitionCount(String topic) {
            List<String> leaders = topicPartitions.get(topic);
            return leaders != null ? leaders.size() : 0;
        }
    }

    // --- 5. Producer ---
    public static class Producer {
        private final ClusterManager clusterManager;

        public Producer(ClusterManager clusterManager) {
            this.clusterManager = clusterManager;
        }

        public void send(String topic, String key, String payload) {
            int partitionCount = clusterManager.getPartitionCount(topic);
            if (partitionCount == 0) {
                throw new RuntimeException("Topic [" + topic + "] does not exist!");
            }

            // Route message to partition using consistent hashing on key
            int partitionId = Math.abs(key.hashCode()) % partitionCount;
            BrokerNode leader = clusterManager.getLeaderForPartition(topic, partitionId);

            PartitionLog partition = leader.getPartition(topic, partitionId);
            Message msg = partition.append(key, payload);
            System.out.println("[Producer] Sent -> Topic: [" + topic + "], Partition: [" + partitionId
                    + "], Broker: [" + leader.getBrokerId() + "] | " + msg);
        }
    }

    // --- 6. Consumer ---
    public static class Consumer {
        private final String consumerGroupId;
        private final ClusterManager clusterManager;
        // Tracks offset per topic-partition
        private final Map<String, Long> consumerOffsets = new ConcurrentHashMap<>();

        public Consumer(String consumerGroupId, ClusterManager clusterManager) {
            this.consumerGroupId = consumerGroupId;
            this.clusterManager = clusterManager;
        }

        public void pollAndProcess(String topic, int partitionId, int maxRecords) {
            BrokerNode leader = clusterManager.getLeaderForPartition(topic, partitionId);
            PartitionLog partition = leader.getPartition(topic, partitionId);

            String offsetKey = topic + "-" + partitionId;
            long currentOffset = consumerOffsets.getOrDefault(offsetKey, 0L);

            List<Message> records = partition.fetch(currentOffset, maxRecords);
            if (!records.isEmpty()) {
                System.out.println("  [Consumer Group: " + consumerGroupId + "] Polled " + records.size()
                        + " messages from Partition [" + partitionId + "]");
                for (Message msg : records) {
                    System.out.println("    Processing -> " + msg);
                    currentOffset = msg.getOffset() + 1;
                }
                consumerOffsets.put(offsetKey, currentOffset);
            }
        }
    }

    // --- 7. Main Method Demonstrating Message Queue Operations ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("   Starting Distributed Message Queue Cluster");
        System.out.println("==================================================\n");

        // Initialize Cluster Brokers
        BrokerNode broker1 = new BrokerNode("Broker-1");
        BrokerNode broker2 = new BrokerNode("Broker-2");
        BrokerNode broker3 = new BrokerNode("Broker-3");

        ClusterManager clusterManager = new ClusterManager();
        clusterManager.registerBroker(broker1);
        clusterManager.registerBroker(broker2);
        clusterManager.registerBroker(broker3);

        // Create a topic with 3 partitions distributed across brokers
        clusterManager.createTopic("order-events", 3, Arrays.asList("Broker-1", "Broker-2", "Broker-3"));

        // Initialize Producer
        Producer producer = new Producer(clusterManager);

        System.out.println("--- Publishing Messages ---");
        producer.send("order-events", "order_101", "Created Order #101");
        producer.send("order-events", "order_102", "Created Order #102");
        producer.send("order-events", "order_103", "Created Order #103");
        producer.send("order-events", "order_104", "Created Order #104");
        producer.send("order-events", "order_105", "Created Order #105");

        System.out.println("\n--- Initializing Consumers & Polling ---");
        Consumer consumerGroupA = new Consumer("analytics-service-group", clusterManager);

        // Poll partitions 0, 1, and 2
        for (int p = 0; p < 3; p++) {
            consumerGroupA.pollAndProcess("order-events", p, 10);
        }

        System.out.println("\n--- Publishing More Messages (Incremental Offset Test) ---");
        producer.send("order-events", "order_106", "Payment Completed for #101");
        producer.send("order-events", "order_107", "Shipped Order #102");

        System.out.println("\n--- Consumers Polling Again for New Records ---");
        for (int p = 0; p < 3; p++) {
            consumerGroupA.pollAndProcess("order-events", p, 10);
        }

        System.out.println("\n==================================================");
        System.out.println("   Distributed Message Queue Demo Completed");
        System.out.println("==================================================\n");
    }
}
