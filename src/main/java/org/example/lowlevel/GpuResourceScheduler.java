package org.example.lowlevel;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class GpuResourceScheduler {

    // --- Task Wrapper Implementing Comparable ---
    public static class GpuTask implements Comparable<GpuTask> {
        private final String taskId;
        private final String tenantId;
        private final int requiredUnits; // e.g., VRAM MB or Compute units required
        private final int priority;      // Higher number = higher priority
        private final Runnable task;

        public GpuTask(String taskId, String tenantId, int requiredUnits, int priority, Runnable task) {
            this.taskId = taskId;
            this.tenantId = tenantId;
            this.requiredUnits = requiredUnits;
            this.priority = priority;
            this.task = task;
        }

        public String getTaskId() { return taskId; }
        public String getTenantId() { return tenantId; }
        public int getRequiredUnits() { return requiredUnits; }
        public int getPriority() { return priority; }
        public Runnable getTask() { return task; }

        @Override
        public int compareTo(GpuTask other) {
            // Descending order: Higher priority values come first
            if (this.priority != other.priority) {
                return Integer.compare(other.priority, this.priority);
            }
            // Secondary sort: Smaller resource footprint first to minimize fragmentation
            return Integer.compare(this.requiredUnits, other.requiredUnits);
        }
    }

    private final int totalGpuUnits;
    private final AtomicInteger availableGpuUnits;
    private final PriorityBlockingQueue<GpuTask> pendingQueue;
    private final ExecutorService executionPool;
    private final ScheduledExecutorService dispatcherLoop;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public GpuResourceScheduler(int totalGpuUnits, int maxConcurrentWorkers) {
        if (totalGpuUnits <= 0 || maxConcurrentWorkers <= 0) {
            throw new IllegalArgumentException("Capacity and worker count must be greater than zero.");
        }
        this.totalGpuUnits = totalGpuUnits;
        this.availableGpuUnits = new AtomicInteger(totalGpuUnits);
        this.pendingQueue = new PriorityBlockingQueue<>();
        this.executionPool = Executors.newFixedThreadPool(maxConcurrentWorkers);
        this.dispatcherLoop = Executors.newSingleThreadScheduledExecutor();

        // Start background dispatcher loop to evaluate resource allocation
        this.dispatcherLoop.scheduleAtFixedRate(this::dispatchTasks, 0, 50, TimeUnit.MILLISECONDS);
    }

    // --- Submit Task ---
    public void submitTask(String taskId, String tenantId, int requiredUnits, int priority, Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("Scheduler is shut down. Cannot accept new tasks.");
        }
        if (requiredUnits > totalGpuUnits) {
            throw new IllegalArgumentException("Task requires [" + requiredUnits +
                    " units], which exceeds total cluster GPU capacity [" + totalGpuUnits + "]");
        }
        GpuTask gpuTask = new GpuTask(taskId, tenantId, requiredUnits, priority, task);
        pendingQueue.put(gpuTask);
        System.out.println("[Submitted] Task [" + taskId + "] from Tenant [" + tenantId +
                "] requiring " + requiredUnits + " units (Priority: " + priority + ")");
    }

    // --- Dispatch Logic ---
    private synchronized void dispatchTasks() {
        if (isShutdown.get() && pendingQueue.isEmpty()) {
            return;
        }

        Iterator<GpuTask> iterator = pendingQueue.iterator();
        List<GpuTask> tasksToRun = new ArrayList<>();

        while (iterator.hasNext()) {
            GpuTask task = iterator.next();
            // Check if enough GPU units are currently free
            if (availableGpuUnits.get() >= task.requiredUnits) {
                // Atomically attempt to reserve units
                if (availableGpuUnits.addAndGet(-task.requiredUnits) >= 0) {
                    iterator.remove();
                    tasksToRun.add(task);
                } else {
                    // Rollback if race condition occurred
                    availableGpuUnits.addAndGet(task.requiredUnits);
                }
            }
        }

        // Submit approved tasks to the execution thread pool
        for (GpuTask task : tasksToRun) {
            executionPool.submit(() -> {
                try {
                    System.out.println("  >>> [Executing] Tenant [" + task.getTenantId() + "] -> Task [" +
                            task.getTaskId() + "] started. Free GPU units remaining: " + availableGpuUnits.get());

                    task.getTask().run();

                    System.out.println("  <<< [Completed] Tenant [" + task.getTenantId() + "] -> Task [" +
                            task.getTaskId() + "] finished.");
                } catch (Exception e) {
                    System.err.println("Error executing task " + task.getTaskId() + ": " + e.getMessage());
                } finally {
                    // Release GPU resources back to the pool
                    int released = availableGpuUnits.addAndGet(task.requiredUnits);
                    System.out.println("[Resources Released] Task [" + task.getTaskId() + "] freed " +
                            task.getRequiredUnits() + " units. Total free GPU units: " + released);
                }
            });
        }
    }

    // --- Graceful Shutdown ---
    public void shutdown() {
        isShutdown.set(true);
        dispatcherLoop.shutdown();
        executionPool.shutdown();
    }

    // --- Main Method for Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        // Initialize cluster with 100 total GPU units and 4 worker threads
        GpuResourceScheduler scheduler = new GpuResourceScheduler(100, 4);

        System.out.println("==================================================");
        System.out.println("   Submitting Multi-Tenant GPU Workloads");
        System.out.println("==================================================\n");

        // Tenant Alpha submits a large task requiring 60 units
        scheduler.submitTask("Task-A1", "Tenant-Alpha", 60, 5, () -> sleep(1200));

        // Tenant Beta submits a high-priority task requiring 40 units
        scheduler.submitTask("Task-B1", "Tenant-Beta", 40, 10, () -> sleep(800));

        // Tenant Gamma submits a task requiring 30 units (should wait since 60 + 40 = 100 capacity is full)
        scheduler.submitTask("Task-C1", "Tenant-Gamma", 30, 5, () -> sleep(600));

        // Tenant Alpha submits a smaller task requiring 30 units (should run alongside Task-B1 once B1 or A1 finishes)
        scheduler.submitTask("Task-A2", "Tenant-Alpha", 30, 2, () -> sleep(500));

        // Allow execution time
        Thread.sleep(3000);

        System.out.println("\n==================================================");
        System.out.println("   Initiating Graceful Scheduler Shutdown");
        System.out.println("==================================================");
        scheduler.shutdown();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
