package org.example.lowlevel;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class HashedWheelTimer {

    // --- Task Wrapper ---
    public static class TimerTask {
        private final Runnable task;
        private final long expirationMs;
        private long remainingRounds;
        private volatile boolean cancelled = false;

        public TimerTask(Runnable task, long delayMs, long tickDurationMs) {
            this.task = task;
            this.expirationMs = System.currentTimeMillis() + delayMs;
            // Calculate how many full rotations of the wheel this task must wait
            long totalTicks = delayMs / tickDurationMs;
            // Will be set by the timer when added to a specific bucket
        }

        public void cancel() {
            this.cancelled = true;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public Runnable getTask() {
            return task;
        }
    }

    // --- Bucket holding tasks for a specific tick slot ---
    private static class Bucket {
        private final ConcurrentLinkedQueue<TimerTask> tasks = new ConcurrentLinkedQueue<>();

        public void addTask(TimerTask task) {
            tasks.add(task);
        }

        public ConcurrentLinkedQueue<TimerTask> getTasks() {
            return tasks;
        }
    }

    private final long tickDurationMs;
    private final int wheelSize;
    private final Bucket[] wheel;
    private int currentTick = 0;

    private final Thread workerThread;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ExecutorService workerPool;

    public HashedWheelTimer(long tickDurationMs, int wheelSize, int workerPoolSize) {
        if (tickDurationMs <= 0 || wheelSize <= 0) {
            throw new IllegalArgumentException("Tick duration and wheel size must be positive.");
        }
        this.tickDurationMs = tickDurationMs;
        this.wheelSize = wheelSize;
        this.wheel = new Bucket[wheelSize];
        for (int i = 0; i < wheelSize; i++) {
            wheel[i] = new Bucket();
        }

        // Thread pool to execute expired tasks asynchronously so the wheel ticker isn't blocked
        this.workerPool = Executors.newFixedThreadPool(workerPoolSize, r -> {
            Thread t = new Thread(r, "Timer-Worker-Pool");
            t.setDaemon(true);
            return t;
        });

        this.workerThread = new Thread(this::runWheel, "HashedWheelTimer-Ticker");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    // --- Schedule Task ---
    public TimerTask schedule(Runnable task, long delayMs) {
        if (!running.get()) {
            throw new IllegalStateException("Timer is shut down.");
        }
        if (delayMs < 0) {
            delayMs = 0;
        }

        TimerTask timerTask = new TimerTask(task, delayMs, tickDurationMs);

        // Calculate total ticks from now
        long ticksAhead = delayMs / tickDurationMs;

        // Calculate wheel index and rounds
        long calculatedRounds = ticksAhead / wheelSize;
        int targetIndex = (int) ((currentTick + ticksAhead) % wheelSize);

        timerTask.remainingRounds = calculatedRounds;
        wheel[targetIndex].addTask(timerTask);

        return timerTask;
    }

    // --- Wheel Ticker Loop ---
    private void runWheel() {
        long startTime = System.currentTimeMillis();
        long tickCount = 0;

        while (running.get()) {
            long expectedTime = startTime + (++tickCount * tickDurationMs);
            long sleepTime = expectedTime - System.currentTimeMillis();

            if (sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    if (!running.get()) {
                        break;
                    }
                    Thread.currentThread().interrupt();
                }
            }

            // Advance wheel tick
            currentTick = (currentTick + 1) % wheelSize;
            Bucket bucket = wheel[currentTick];

            // Process bucket tasks
            for (TimerTask task : bucket.getTasks()) {
                if (task.isCancelled()) {
                    bucket.getTasks().remove(task);
                    continue;
                }

                if (task.remainingRounds > 0) {
                    task.remainingRounds--;
                } else {
                    // Rounds expired, execute task
                    bucket.getTasks().remove(task);
                    workerPool.submit(() -> {
                        try {
                            task.getTask().run();
                        } catch (Exception e) {
                            System.err.println("Task execution failed: " + e.getMessage());
                        }
                    });
                }
            }
        }
    }

    // --- Shutdown ---
    public void shutdown() {
        running.set(false);
        workerThread.interrupt();
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(2, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Timer service shut down successfully.");
    }

    // --- Main Method Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        // Create a timer with a 100ms tick duration and a wheel size of 60 slots (6 seconds total wheel cycle)
        HashedWheelTimer timer = new HashedWheelTimer(100, 60, 4);

        System.out.println("Scheduling tasks at " + System.currentTimeMillis());

        // Schedule quick task (200ms delay)
        timer.schedule(() -> System.out.println("-> Executed Task 1 (200ms delay)"), 200);

        // Schedule mid task (1000ms delay)
        timer.schedule(() -> System.out.println("-> Executed Task 2 (1000ms delay)"), 1000);

        // Schedule task to be cancelled
        TimerTask cancelledTask = timer.schedule(() -> System.out.println("-> [ERROR] This should not run!"), 500);
        cancelledTask.cancel();
        System.out.println("Task 3 cancelled successfully.");

        // Schedule long task spanning multiple rounds (7000ms delay with a 6-second wheel size)
        timer.schedule(() -> System.out.println("-> Executed Task 4 (7000ms multi-round delay)"), 7000);

        // Let tasks run for 8 seconds
        Thread.sleep(8000);

        // Shutdown timer
        timer.shutdown();
    }
}
