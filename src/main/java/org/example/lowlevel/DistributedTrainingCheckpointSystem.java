package org.example.lowlevel;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedTrainingCheckpointSystem {

    // --- 1. Checkpoint Metadata & Versioning ---
    public static class CheckpointMetadata {
        private final long checkpointId;
        private final int epoch;
        private final long globalStep;
        private final Map<String, String> workerFiles;

        public CheckpointMetadata(long checkpointId, int epoch, long globalStep, Map<String, String> workerFiles) {
            this.checkpointId = checkpointId;
            this.epoch = epoch;
            this.globalStep = globalStep;
            this.workerFiles = workerFiles;
        }

        public long getCheckpointId() { return checkpointId; }
        public int getEpoch() { return epoch; }
        public long getGlobalStep() { return globalStep; }
        public Map<String, String> getWorkerFiles() { return workerFiles; }
    }

    // --- 2. Worker Training State Representation ---
    public static class WorkerState {
        private final String workerId;
        private int epoch;
        private long globalStep;
        private final Map<String, Double> modelWeights;
        private final Map<String, Double> optimizerState;

        public WorkerState(String workerId) {
            this.workerId = workerId;
            this.modelWeights = new ConcurrentHashMap<>();
            this.optimizerState = new ConcurrentHashMap<>();
        }

        public String getWorkerId() { return workerId; }
        public synchronized int getEpoch() { return epoch; }
        public synchronized void setEpoch(int epoch) { this.epoch = epoch; }
        public synchronized long getGlobalStep() { return globalStep; }
        public synchronized void setGlobalStep(long globalStep) { this.globalStep = globalStep; }
        public Map<String, Double> getModelWeights() { return modelWeights; }
        public Map<String, Double> getOptimizerState() { return optimizerState; }

        public synchronized void updateTraining(int epoch, long step, String paramKey, double weightVal, double optVal) {
            this.epoch = epoch;
            this.globalStep = step;
            this.modelWeights.put(paramKey, weightVal);
            this.optimizerState.put(paramKey, optVal);
        }
    }

    // --- 3. Local Checkpointer / Storage Simulator ---
    public static class LocalCheckpointer {
        private final Map<Long, Map<String, WorkerState>> storage = new ConcurrentHashMap<>();

        public void saveCheckpointShard(long checkpointId, String workerId, WorkerState state) {
            storage.computeIfAbsent(checkpointId, k -> new ConcurrentHashMap<>()).put(workerId, deepCopy(state));
            System.out.println("  [Storage] Worker [" + workerId + "] successfully persisted checkpoint shard for C-ID: " + checkpointId);
        }

        public WorkerState loadCheckpointShard(long checkpointId, String workerId) {
            Map<String, WorkerState> shardMap = storage.get(checkpointId);
            if (shardMap == null || !shardMap.containsKey(workerId)) {
                throw new RuntimeException("Checkpoint shard not found for checkpoint " + checkpointId + " and worker " + workerId);
            }
            return deepCopy(shardMap.get(workerId));
        }

        private WorkerState deepCopy(WorkerState source) {
            WorkerState copy = new WorkerState(source.getWorkerId());
            copy.setEpoch(source.getEpoch());
            copy.setGlobalStep(source.getGlobalStep());
            copy.getModelWeights().putAll(source.getModelWeights());
            copy.getOptimizerState().putAll(source.getOptimizerState());
            return copy;
        }
    }

    // --- 4. Checkpoint Coordinator ---
    public static class CheckpointCoordinator {
        private final LocalCheckpointer checkpointer;
        private final List<WorkerNode> workers;
        private final NavigableMap<Long, CheckpointMetadata> globalRegistry = new ConcurrentSkipListMap<>();

        public CheckpointCoordinator(LocalCheckpointer checkpointer, List<WorkerNode> workers) {
            this.checkpointer = checkpointer;
            this.workers = workers;
        }

        public CheckpointMetadata createGlobalCheckpoint(int epoch, long globalStep) {
            long checkpointId = System.currentTimeMillis();
            System.out.println("\n[Coordinator] Initiating Global Checkpoint [ID: " + checkpointId + ", Epoch: " + epoch + ", Step: " + globalStep + "]...");

            Map<String, String> workerFiles = new ConcurrentHashMap<>();
            CountDownLatch latch = new CountDownLatch(workers.size());
            ExecutorService executor = Executors.newFixedThreadPool(workers.size());

            for (WorkerNode worker : workers) {
                executor.submit(() -> {
                    try {
                        WorkerState state = worker.getStateSnapshot();
                        checkpointer.saveCheckpointShard(checkpointId, worker.getWorkerId(), state);
                        workerFiles.put(worker.getWorkerId(), "shard_" + worker.getWorkerId() + "_" + checkpointId + ".ckpt");
                    } catch (Exception e) {
                        System.err.println("[Coordinator] Failed to save shard for worker: " + worker.getWorkerId());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executor.shutdown();

            CheckpointMetadata metadata = new CheckpointMetadata(checkpointId, epoch, globalStep, workerFiles);
            globalRegistry.put(checkpointId, metadata);
            System.out.println("[Coordinator] Global Checkpoint " + checkpointId + " successfully committed.\n");
            return metadata;
        }

        public CheckpointMetadata getLatestCheckpoint() {
            if (globalRegistry.isEmpty()) return null;
            return globalRegistry.lastEntry().getValue();
        }

        public void recoverCluster() {
            CheckpointMetadata latest = getLatestCheckpoint();
            if (latest == null) {
                System.out.println("[Coordinator] No checkpoints found to recover from!");
                return;
            }
            System.out.println("\n[Coordinator] Restoring cluster state from latest Checkpoint ID: " + latest.getCheckpointId() + " (Epoch: " + latest.getEpoch() + ", Step: " + latest.getGlobalStep() + ")");

            CountDownLatch latch = new CountDownLatch(workers.size());
            ExecutorService executor = Executors.newFixedThreadPool(workers.size());

            for (WorkerNode worker : workers) {
                executor.submit(() -> {
                    try {
                        WorkerState restoredState = checkpointer.loadCheckpointShard(latest.getCheckpointId(), worker.getWorkerId());
                        worker.restoreState(restoredState);
                        System.out.println("  [Recovery] Worker [" + worker.getWorkerId() + "] successfully restored state.");
                    } catch (Exception e) {
                        System.err.println("  [Recovery] Failed to restore worker " + worker.getWorkerId() + ": " + e.getMessage());
                    } finally {
                        latch.countDown();
                    }
                });
            }

            try {
                latch.await(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executor.shutdown();
            System.out.println("[Coordinator] Cluster recovery completed successfully.\n");
        }
    }

    // --- 5. Worker Node ---
    public static class WorkerNode {
        private final String workerId;
        private final WorkerState state;
        private volatile boolean active = true;

        public WorkerNode(String workerId) {
            this.workerId = workerId;
            this.state = new WorkerState(workerId);
        }

        public String getWorkerId() { return workerId; }
        public boolean isActive() { return active; }
        public void setActive(boolean active) { this.active = active; }

        public synchronized WorkerState getStateSnapshot() {
            if (!active) throw new RuntimeException("Worker " + workerId + " is dead!");
            return state;
        }

        public synchronized void restoreState(WorkerState restoredState) {
            this.state.setEpoch(restoredState.getEpoch());
            this.state.setGlobalStep(restoredState.getGlobalStep());
            this.state.getModelWeights().clear();
            this.state.getModelWeights().putAll(restoredState.getModelWeights());
            this.state.getOptimizerState().clear();
            this.state.getOptimizerState().putAll(restoredState.getOptimizerState());
        }

        public void trainStep(int epoch, long step, String paramKey, double weightVal, double optVal) {
            if (!active) throw new RuntimeException("Worker " + workerId + " cannot train; node is down!");
            state.updateTraining(epoch, step, paramKey, weightVal, optVal);
            System.out.println("  [" + workerId + "] Trained epoch " + epoch + ", step " + step + " -> param [" + paramKey + "] = " + weightVal);
        }
    }

    // --- 6. Demonstration Main Method ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("   Starting Distributed Training Checkpoint System");
        System.out.println("==================================================\n");

        LocalCheckpointer checkpointer = new LocalCheckpointer();

        WorkerNode worker1 = new WorkerNode("Worker-1");
        WorkerNode worker2 = new WorkerNode("Worker-2");
        List<WorkerNode> workers = Arrays.asList(worker1, worker2);

        CheckpointCoordinator coordinator = new CheckpointCoordinator(checkpointer, workers);

        // 1. Simulate Training Steps & Periodic Checkpoint
        System.out.println("--- Phase 1: Training Iterations & First Checkpoint ---");
        worker1.trainStep(1, 100, "layer1.weight", 0.45, 0.01);
        worker2.trainStep(1, 100, "layer2.weight", 0.78, 0.02);

        coordinator.createGlobalCheckpoint(1, 100);

        // 2. Further Training Iterations
        System.out.println("--- Phase 2: Continuing Training ---");
        worker1.trainStep(1, 200, "layer1.weight", 0.52, 0.015);
        worker2.trainStep(1, 200, "layer2.weight", 0.81, 0.025);

        coordinator.createGlobalCheckpoint(1, 200);

        // 3. Simulate Worker Crash & Cluster Recovery
        System.out.println("*** SIMULATING CATASTROPHIC NODE FAILURE (Worker-1 Crashed) ***");
        worker1.setActive(false);

        try {
            worker1.trainStep(2, 300, "layer1.weight", 0.60, 0.02);
        } catch (Exception e) {
            System.err.println("Caught Expected Error: " + e.getMessage() + "\n");
        }

        System.out.println("*** RECOVERING CLUSTER FROM LATEST VALID CHECKPOINT ***");
        worker1.setActive(true);
        coordinator.recoverCluster();

        // 4. Verify Restored State on Worker-1
        System.out.println("--- Verifying Restored State ---");
        WorkerState w1State = worker1.getStateSnapshot();
        System.out.println("Worker-1 Restored Epoch: " + w1State.getEpoch());
        System.out.println("Worker-1 Restored Global Step: " + w1State.getGlobalStep());
        System.out.println("Worker-1 Restored Weights: " + w1State.getModelWeights());

        System.out.println("\n==================================================");
        System.out.println("   Distributed Training Checkpoint Demo Completed");
        System.out.println("==================================================");
    }
}