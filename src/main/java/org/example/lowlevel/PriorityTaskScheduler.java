package org.example.lowlevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class PriorityTaskScheduler {

    // --- Task Wrapper Implementing Comparable ---
    public static class PriorityTask implements Comparable<PriorityTask> {
        private final String name;
        private final int priority; // Higher number = higher priority
        private final Runnable task;

        public PriorityTask(String name, int priority, Runnable task) {
            this.name = name;
            this.priority = priority;
            this.task = task;
        }

        public String getName() {
            return name;
        }

        public int getPriority() {
            return priority;
        }

        public Runnable getTask() {
            return task;
        }

        @Override
        public int compareTo(PriorityTask other) {
            // Descending order: Higher priority values come first in the queue
            return Integer.compare(other.priority, this.priority);
        }
    }

    private final int corePoolSize;
    private final PriorityBlockingQueue<PriorityTask> taskQueue;
    private final List<WorkerThread> workers;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public PriorityTaskScheduler(int corePoolSize) {
        if (corePoolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be greater than 0");
        }
        this.corePoolSize = corePoolSize;
        this.taskQueue = new PriorityBlockingQueue<>();
        this.workers = new ArrayList<>(corePoolSize);

        // Initialize and start worker threads
        for (int i = 0; i < corePoolSize; i++) {
            WorkerThread worker = new WorkerThread("Scheduler-Worker-" + i, taskQueue, isShutdown);
            workers.add(worker);
            worker.start();
        }
    }

    // --- Submit Task with Priority ---
    public void submit(String name, int priority, Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("Scheduler is shut down. Cannot accept new tasks.");
        }
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        PriorityTask priorityTask = new PriorityTask(name, priority, task);
        taskQueue.put(priorityTask);
    }

    // --- Graceful Shutdown ---
    public void shutdown() {
        isShutdown.set(true);
        for (WorkerThread worker : workers) {
            worker.interrupt();
        }
    }

    // --- Main Method for Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        // Create a scheduler with 2 worker threads
        PriorityTaskScheduler scheduler = new PriorityTaskScheduler(2);

        // Submit tasks with varying priorities (Higher number = Higher Priority)
        scheduler.submit("Low-Task-1", 1, () -> {
            System.out.println("-> Executing Low Priority Task 1");
            sleep(400);
        });

        scheduler.submit("High-Task-1", 10, () -> {
            System.out.println("-> Executing High Priority Task 1");
            sleep(400);
        });

        scheduler.submit("Medium-Task-1", 5, () -> {
            System.out.println("-> Executing Medium Priority Task 1");
            sleep(400);
        });

        scheduler.submit("High-Task-2", 10, () -> {
            System.out.println("-> Executing High Priority Task 2");
            sleep(400);
        });

        scheduler.submit("Low-Task-2", 1, () -> {
            System.out.println("-> Executing Low Priority Task 2");
            sleep(400);
        });

        // Allow tasks time to run before initiating shutdown
        Thread.sleep(2500);

        System.out.println("\nInitiating graceful shutdown...");
        scheduler.shutdown();
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // --- Worker Thread Definition ---
    private static class WorkerThread extends Thread {
        private final PriorityBlockingQueue<PriorityTask> taskQueue;
        private final AtomicBoolean isShutdown;

        public WorkerThread(String name, PriorityBlockingQueue<PriorityTask> taskQueue, AtomicBoolean isShutdown) {
            super(name);
            this.taskQueue = taskQueue;
            this.isShutdown = isShutdown;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    PriorityTask priorityTask;
                    if (isShutdown.get()) {
                        priorityTask = taskQueue.poll();
                        if (priorityTask == null) {
                            break; // Exit when queue is drained during shutdown
                        }
                    } else {
                        priorityTask = taskQueue.take(); // Blocks until a task is available
                    }

                    // Execute task safely
                    try {
                        System.out.println("[" + getName() + "] Started: " + priorityTask.getName()
                                + " (Priority: " + priorityTask.getPriority() + ")");
                        priorityTask.getTask().run();
                        System.out.println("[" + getName() + "] Completed: " + priorityTask.getName());
                    } catch (RuntimeException e) {
                        System.err.println("Exception caught in task execution: " + e.getMessage());
                    }

                } catch (InterruptedException e) {
                    if (isShutdown.get() && taskQueue.isEmpty()) {
                        break;
                    }
                }
            }
        }
    }
}
