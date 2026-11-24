package org.example.lowlevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
/**
 * Provides the custom thread pool implementation used to demonstrate low-level concurrency patterns.
 */


public class CustomThreadPool {

    // Task queue to hold submitted tasks
    private final BlockingQueue<Runnable> taskQueue;
    // List of worker threads that will execute tasks
    private final List<WorkerThread> workers;
    // AtomicBoolean to indicate if the pool is shutting down
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    /*
     * Worker Thread Definition
     */
    private static class WorkerThread extends Thread {
        // Fields

        // Task queue from which the worker will fetch tasks
        private final BlockingQueue<Runnable> taskQueue;
        // AtomicBoolean to check if the pool is shutting down
        private final AtomicBoolean isShutdown;

        public WorkerThread(String name, BlockingQueue<Runnable> taskQueue, AtomicBoolean isShutdown) {
            super(name);
            this.taskQueue = taskQueue;
            this.isShutdown = isShutdown;
        }

        /*
         * The run method continuously fetches and executes tasks from the queue.
         * It handles interruptions and ensures that exceptions in task execution do not terminate the worker.
         */
        @Override
        public void run() {
            while (true) {
                try {
                    Runnable task;
                    if (isShutdown.get()) {
                        // During shutdown, process remaining tasks in the queue before exiting
                        task = taskQueue.poll();
                        if (task == null) {
                            break; // Queue is empty and shutdown is active, exit thread
                        }
                    } else {
                        // Block until a new task becomes available
                        task = taskQueue.take();
                    }

                    // Execute the task while isolating exceptions to prevent worker death
                    try {
                        task.run();
                    } catch (RuntimeException e) {
                        System.err.println("Exception caught in task execution: " + e.getMessage());
                    }

                } catch (InterruptedException e) {
                    // If interrupted, check if shutdown was requested with an empty queue
                    if (isShutdown.get() && taskQueue.isEmpty()) {
                        break;
                    }
                }
            }
        }
    }

    public CustomThreadPool(int corePoolSize, int queueCapacity) {
        if (corePoolSize <= 0 || queueCapacity <= 0) {
            throw new IllegalArgumentException("Pool size and queue capacity must be greater than 0");
        }
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.workers = new ArrayList<>(corePoolSize);

        // Initialize and start worker threads
        for (int i = 1; i <= corePoolSize; i++) {
            WorkerThread worker = new WorkerThread("ThreadPool-Worker-" + i, taskQueue, isShutdown);
            workers.add(worker);
            worker.start();
        }
    }

    /*
     * Submits a task to the thread pool.
     * If the pool is shutting down, it throws an IllegalStateException.
     * If the task is null, it throws an IllegalArgumentException.
     * If the queue is full, it blocks until space becomes available (backpressure).
     */
    public void submit(Runnable task) {
        if (isShutdown.get()) {
            throw new IllegalStateException("ThreadPool is shut down. Cannot accept new tasks.");
        }
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        try {
            // Blocks if the queue is full (backpressure)
            taskQueue.put(task);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while submitting task", e);
        }
    }

    /*
     * Gracefully shuts down the thread pool.
     * Interrupts all worker threads and prevents new tasks from being submitted.
     */
    public void shutdown() {
        isShutdown.set(true);
        // Wake up worker threads that might be blocked waiting for tasks
        for (WorkerThread worker : workers) {
            worker.interrupt();
        }
    }

    /*
     * Main method for demonstration purposes.
     */
    public static void main(String[] args) throws InterruptedException {
        // Create a thread pool with 5 worker threads and a queue capacity of 15
        CustomThreadPool pool = new CustomThreadPool(5, 15);

        // Submit 50 tasks to the pool
        for (int i = 1; i <= 50; i++) {
            final int taskId = i;
            pool.submit(() -> {
                String threadName = Thread.currentThread().getName();
                System.out.println("Task " + taskId + " started by " + threadName);
                try {
                    Thread.sleep(600); // Simulate processing time
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                System.out.println("Task " + taskId + " completed by " + threadName);
            });
        }

        // Allow time for tasks to complete before shutting down
        Thread.sleep(2500);

        System.out.println("\nInitiating graceful shutdown...");
        pool.shutdown();
    }


}
