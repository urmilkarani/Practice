package org.example.lowlevel;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class AsyncLogger {

    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    public static class LogMessage {
        private final Instant timestamp;
        private final String threadName;
        private final LogLevel level;
        private final String message;

        public LogMessage(LogLevel level, String message) {
            this.timestamp = Instant.now();
            this.threadName = Thread.currentThread().getName();
            this.level = level;
            this.message = message;
        }

        public Instant getTimestamp() { return timestamp; }
        public String getThreadName() { return threadName; }
        public LogLevel getLevel() { return level; }
        public String getMessage() { return message; }
    }

    private final BlockingQueue<LogMessage> queue;
    private final Thread workerThread;
    private volatile boolean running = true;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_INSTANT;

    public AsyncLogger(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.workerThread = new Thread(this::processLogs, "AsyncLogger-Worker");
        this.workerThread.setDaemon(true);
        this.workerThread.start();
    }

    // --- Background Worker Consumer Loop ---
    private void processLogs() {
        try {
            while (running || !queue.isEmpty()) {
                LogMessage log = queue.poll(500, TimeUnit.MILLISECONDS);
                if (log != null) {
                    writeLog(log);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Final drain of any leftover messages after shutdown signal
        LogMessage log;
        while ((log = queue.poll()) != null) {
            writeLog(log);
        }
    }

    // --- Simulated Sink Writer (e.g., Console or File) ---
    private void writeLog(LogMessage log) {
        String formatted = String.format("[%s] [%s] [%s] - %s",
                FORMATTER.format(log.getTimestamp()),
                log.getThreadName(),
                log.getLevel(),
                log.getMessage());
        System.out.println(formatted);
    }

    // --- Public Logging API (Producers) ---
    public void log(LogLevel level, String message) {
        if (!running) {
            System.err.println("Logger is shut down. Dropping log: " + message);
            return;
        }

        LogMessage logMessage = new LogMessage(level, message);
        try {
            // queue.put() applies backpressure by blocking producer if the queue is full
            queue.put(logMessage);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Log submission interrupted.");
        }
    }

    public void info(String message) { log(LogLevel.INFO, message); }
    public void debug(String message) { log(LogLevel.DEBUG, message); }
    public void warn(String message) { log(LogLevel.WARN, message); }
    public void error(String message) { log(LogLevel.ERROR, message); }

    // --- Graceful Shutdown ---
    public void shutdown() throws InterruptedException {
        running = false;
        workerThread.join(); // Wait for worker thread to drain remaining logs
        System.out.println("AsyncLogger shut down successfully.");
    }

    // --- Demonstration Main Method ---
    public static void main(String[] args) throws InterruptedException {
        // Initialize logger with a queue capacity of 50
        AsyncLogger logger = new AsyncLogger(50);

        // Create multiple concurrent application threads generating logs
        Thread[] appThreads = new Thread[3];
        for (int i = 0; i < appThreads.length; i++) {
            final int threadId = i + 1;
            appThreads[i] = new Thread(() -> {
                for (int j = 1; j <= 5; j++) {
                    logger.info("Processing task step " + j);
                    logger.debug("Detailed debug info for step " + j);
                    try {
                        Thread.sleep(30); // Simulate application work
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }, "AppWorker-" + threadId);
        }

        // Start all producer threads
        for (Thread t : appThreads) {
            t.start();
        }

        // Wait for application threads to finish logging
        for (Thread t : appThreads) {
            t.join();
        }

        // Allow a brief pause for queue drainage and shut down
        Thread.sleep(100);
        logger.shutdown();
    }
}
