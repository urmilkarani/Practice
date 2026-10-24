package org.example.lowlevel;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DistributedJobScheduler {

    // --- Job Status Lifecycle ---
    public enum JobStatus {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED
    }

    // --- Distributed Job Representation ---
    public static class Job {
        private final String jobId;
        private final Runnable task;
        private final int maxRetries;
        private int retryCount = 0;
        private JobStatus status = JobStatus.PENDING;
        private String assignedNodeId = null;

        public Job(String jobId, Runnable task, int maxRetries) {
            this.jobId = jobId;
            this.task = task;
            this.maxRetries = maxRetries;
        }

        public String getJobId() { return jobId; }
        public Runnable getTask() { return task; }
        public synchronized JobStatus getStatus() { return status; }
        public synchronized void setStatus(JobStatus status) { this.status = status; }
        public synchronized String getAssignedNodeId() { return assignedNodeId; }
        public synchronized void setAssignedNodeId(String nodeId) { this.assignedNodeId = nodeId; }
        public synchronized int getRetryCount() { return retryCount; }
        public synchronized void incrementRetryCount() { this.retryCount++; }
        public int getMaxRetries() { return maxRetries; }
    }

    // --- Centralized Job Repository (Simulating Distributed Storage like Cassandra/PostgreSQL) ---
    public static class JobRepository {
        private final Map<String, Job> jobs = new ConcurrentHashMap<>();

        public void saveJob(Job job) {
            jobs.put(job.getJobId(), job);
        }

        public Job getJob(String jobId) {
            return jobs.get(jobId);
        }

        public List<Job> getJobsByStatus(JobStatus status) {
            List<Job> result = new ArrayList<>();
            for (Job job : jobs.values()) {
                if (job.getStatus() == status) {
                    result.add(job);
                }
            }
            return result;
        }

        public List<Job> getJobsByNodeAndStatus(String nodeId, JobStatus status) {
            List<Job> result = new ArrayList<>();
            for (Job job : jobs.values()) {
                if (nodeId.equals(job.getAssignedNodeId()) && job.getStatus() == status) {
                    result.add(job);
                }
            }
            return result;
        }
    }

    // --- Cluster Coordinator (Heartbeat & Worker Failure Detector) ---
    public static class ClusterCoordinator {
        private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
        private final long heartbeatTimeoutMs;
        private final JobRepository jobRepository;

        public ClusterCoordinator(long heartbeatTimeoutMs, JobRepository jobRepository) {
            this.heartbeatTimeoutMs = heartbeatTimeoutMs;
            this.jobRepository = jobRepository;
        }

        public void registerHeartbeat(String nodeId) {
            lastHeartbeat.put(nodeId, System.currentTimeMillis());
        }

        public List<String> getActiveNodes() {
            List<String> active = new ArrayList<>();
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : lastHeartbeat.entrySet()) {
                if (now - entry.getValue() <= heartbeatTimeoutMs) {
                    active.add(entry.getKey());
                }
            }
            return active;
        }

        // Detect node failures and reassign orphan tasks
        public void detectAndHandleFailures() {
            long now = System.currentTimeMillis();
            for (Map.Entry<String, Long> entry : lastHeartbeat.entrySet()) {
                String nodeId = entry.getKey();
                long lastTime = entry.getValue();

                if (now - lastTime > heartbeatTimeoutMs) {
                    System.err.println("ALERT: Worker Node [" + nodeId + "] dead/unresponsive! Initiating failover recovery...");

                    // Recover orphan running jobs
                    List<Job> orphanJobs = jobRepository.getJobsByNodeAndStatus(nodeId, JobStatus.RUNNING);
                    for (Job job : orphanJobs) {
                        if (job.getRetryCount() < job.getMaxRetries()) {
                            job.incrementRetryCount();
                            job.setStatus(JobStatus.PENDING);
                            job.setAssignedNodeId(null);
                            System.out.println("-> Re-queued job [" + job.getJobId() + "] for retry ("
                                    + job.getRetryCount() + "/" + job.getMaxRetries() + ")");
                        } else {
                            job.setStatus(JobStatus.FAILED);
                            System.err.println("-> Job [" + job.getJobId() + "] exceeded max retries and marked FAILED.");
                        }
                    }
                    // Remove node from heartbeats
                    lastHeartbeat.remove(nodeId);
                }
            }
        }
    }

    // --- Master Scheduler (Job Dispatcher) ---
    public static class MasterNode {
        private final JobRepository jobRepository;
        private final ClusterCoordinator coordinator;
        private final ScheduledExecutorService masterLoop = Executors.newSingleThreadScheduledExecutor();
        private final AtomicInteger roundRobinIndex = new AtomicInteger(0);

        public MasterNode(JobRepository jobRepository, ClusterCoordinator coordinator) {
            this.jobRepository = jobRepository;
            this.coordinator = coordinator;
        }

        public void start() {
            // Schedule background coordination and dispatch loops
            masterLoop.scheduleAtFixedRate(this::dispatchJobs, 0, 500, TimeUnit.MILLISECONDS);
            masterLoop.scheduleAtFixedRate(coordinator::detectAndHandleFailures, 0, 1000, TimeUnit.MILLISECONDS);
        }

        private void dispatchJobs() {
            List<String> activeNodes = coordinator.getActiveNodes();
            if (activeNodes.isEmpty()) {
                return;
            }

            List<Job> pendingJobs = jobRepository.getJobsByStatus(JobStatus.PENDING);
            for (Job job : pendingJobs) {
                // Round-Robin Load Balancing across active cluster nodes
                int index = Math.abs(roundRobinIndex.getAndIncrement() % activeNodes.size());
                String targetNode = activeNodes.get(index);

                job.setAssignedNodeId(targetNode);
                job.setStatus(JobStatus.RUNNING);
                System.out.println("[Master] Dispatched Job [" + job.getJobId() + "] -> Worker Node [" + targetNode + "]");
            }
        }

        public void stop() {
            masterLoop.shutdownNow();
        }
    }

    // --- Worker Node (Executes Jobs & Sends Heartbeats) ---
    public static class WorkerNode {
        private final String nodeId;
        private final JobRepository jobRepository;
        private final ClusterCoordinator coordinator;
        private final ExecutorService taskExecutor = Executors.newFixedThreadPool(2);
        private final ScheduledExecutorService heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        private final ScheduledExecutorService pollExecutor = Executors.newSingleThreadScheduledExecutor();
        private final AtomicBoolean active = new AtomicBoolean(true);

        public WorkerNode(String nodeId, JobRepository jobRepository, ClusterCoordinator coordinator) {
            this.nodeId = nodeId;
            this.jobRepository = jobRepository;
            this.coordinator = coordinator;
        }

        public void start() {
            // Send heartbeats every 500ms
            heartbeatExecutor.scheduleAtFixedRate(() -> {
                if (active.get()) {
                    coordinator.registerHeartbeat(nodeId);
                }
            }, 0, 500, TimeUnit.MILLISECONDS);

            // Poll for assigned jobs every 300ms
            pollExecutor.scheduleAtFixedRate(this::pollAndExecuteJobs, 0, 300, TimeUnit.MILLISECONDS);
        }

        private void pollAndExecuteJobs() {
            if (!active.get()) return;

            List<Job> myJobs = jobRepository.getJobsByNodeAndStatus(nodeId, JobStatus.RUNNING);
            for (Job job : myJobs) {
                taskExecutor.submit(() -> {
                    try {
                        System.out.println("  [" + nodeId + "] Executing Job: " + job.getJobId());
                        job.getTask().run();
                        job.setStatus(JobStatus.COMPLETED);
                        System.out.println("  [" + nodeId + "] Successfully Completed Job: " + job.getJobId());
                    } catch (Exception e) {
                        System.err.println("  [" + nodeId + "] Execution failed for Job: " + job.getJobId());
                        if (job.getRetryCount() < job.getMaxRetries()) {
                            job.incrementRetryCount();
                            job.setStatus(JobStatus.PENDING);
                            job.setAssignedNodeId(null);
                        } else {
                            job.setStatus(JobStatus.FAILED);
                        }
                    }
                });
            }
        }

        // Simulate abrupt worker crash
        public void simulateCrash() {
            System.out.println("\n*** SIMULATING CRASH OF WORKER NODE: [" + nodeId + "] ***\n");
            active.set(false);
            heartbeatExecutor.shutdownNow();
            pollExecutor.shutdownNow();
            taskExecutor.shutdownNow();
        }

        public void stop() {
            active.set(false);
            heartbeatExecutor.shutdown();
            pollExecutor.shutdown();
            taskExecutor.shutdown();
        }
    }

    // --- Main Method Demonstrating Cluster Lifecycle & Failover ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("==================================================");
        System.out.println("   Starting Distributed Job Scheduler Cluster");
        System.out.println("==================================================\n");

        JobRepository repository = new JobRepository();
        ClusterCoordinator coordinator = new ClusterCoordinator(1500, repository); // 1.5s timeout for heartbeats

        // Instantiate Master
        MasterNode master = new MasterNode(repository, coordinator);
        master.start();

        // Instantiate Worker Nodes
        WorkerNode worker1 = new WorkerNode("Worker-A", repository, coordinator);
        WorkerNode worker2 = new WorkerNode("Worker-B", repository, coordinator);
        worker1.start();
        worker2.start();

        // Allow workers time to register initial heartbeats
        Thread.sleep(500);

        // Submit Jobs to Repository
        System.out.println("--- Submitting Initial Batch of Jobs ---");
        repository.saveJob(new Job("JOB-101", () -> sleep(400), 2));
        repository.saveJob(new Job("JOB-102", () -> sleep(400), 2));
        repository.saveJob(new Job("JOB-103", () -> sleep(800), 2));

        Thread.sleep(1000);

        // Submit a long-running job and crash the node executing it
        System.out.println("\n--- Submitting Long-Running Job for Failover Test ---");
        Job longJob = new Job("JOB-999-FAILOVER", () -> {
            System.out.println("  [JOB-999] Processing critical payload...");
            sleep(3000); // Exceeds worker crash timing
        }, 3);
        repository.saveJob(longJob);

        // Wait for master to assign JOB-999
        Thread.sleep(600);

        // Simulate crash on Worker-A
        if ("Worker-A".equals(longJob.getAssignedNodeId())) {
            worker1.simulateCrash();
        } else {
            worker2.simulateCrash();
        }

        // Observe Coordinator detecting failure and reassigning the job to the surviving worker
        Thread.sleep(4000);

        // Graceful shutdown
        System.out.println("\n==================================================");
        System.out.println("   Shutting down Distributed Scheduler Cluster");
        System.out.println("==================================================");
        master.stop();
        worker1.stop();
        worker2.stop();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
