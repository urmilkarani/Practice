package org.example.lowlevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class CustomThreadPool {
    private final int corePoolSize;
    private final BlockingQueue<Runnable> taskQueue;
    private final List<WorkerThread> workers;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);

    public CustomThreadPool(int corePoolSize, int queueCapacity) {
        if (corePoolSize <= 0 || queueCapacity <= 0) {
            throw new IllegalArgumentException("Pool size and queue capacity must be greater than 0");
        }
        this.corePoolSize = corePoolSize;
        this.taskQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.workers = new ArrayList<>(corePoolSize);

        // Initialize and start worker threads
        for (int i = 0; i < corePoolSize; i++) {
            WorkerThread worker = new WorkerThread("ThreadPool-Worker-" + i, taskQueue, isShutdown);
            workers.add(worker);
            worker.start();
        }
    }

    // --- Submit Task ---
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

    // --- Graceful Shutdown ---
    public void shutdown() {
        isShutdown.set(true);
        // Wake up worker threads that might be blocked waiting for tasks
        for (WorkerThread worker : workers) {
            worker.interrupt();
        }
    }

    // --- Main Method for Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        // Create a thread pool with 3 worker threads and a queue capacity of 5
        CustomThreadPool pool = new CustomThreadPool(3, 5);

        // Submit 6 tasks to the pool
        for (int i = 1; i <= 6; i++) {
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

    // --- Worker Thread Definition ---
    private static class WorkerThread extends Thread {
        private final BlockingQueue<Runnable> taskQueue;
        private final AtomicBoolean isShutdown;

        public WorkerThread(String name, BlockingQueue<Runnable> taskQueue, AtomicBoolean isShutdown) {
            super(name);
            this.taskQueue = taskQueue;
            this.isShutdown = isShutdown;
        }

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
}
