package org.example.lowlevel;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/*
 * This is a simple asynchronous logger implementation that uses a bounded blocking queue to decouple log producers from the log consumer.
 * It supports multiple log levels and ensures that log messages are processed in the order they were submitted.
 * The logger runs a background worker thread that continuously polls the queue for new log messages and writes them to the sink (console in this case).
 * If the queue is full, producers will block until space becomes available, providing backpressure.
 */
public class AsyncLogger {

    /**
     * Log levels for categorizing log messages.
     */
    public enum LogLevel {
        DEBUG, INFO, WARN, ERROR
    }

    /**
     * Represents a log message with its associated metadata.
     */
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

    // --- Core Components ---

    // Bounded blocking queue to hold log messages
    private final BlockingQueue<LogMessage> queue;
    // Background worker thread that consumes log messages
    private final Thread workerThread;
    // Flag to indicate if the logger is still running
    private volatile boolean running = true;
    // Formatter for consistent timestamp output
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

    /**
     * Continuously processes log messages from the queue and writes them to the sink.
     * This method runs in the background worker thread.
     */
    private void processLogs() {
        try {
            // Continuously poll the queue for log messages until shutdown is signaled
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

    /**
     * Writes a single log message to the sink (console).
     * @param log the log message to write
     */
    private void writeLog(LogMessage log) {
        String formatted = String.format("[%s] [%s] [%s] - %s",
                FORMATTER.format(log.getTimestamp()),
                log.getThreadName(),
                log.getLevel(),
                log.getMessage());
        System.out.println(formatted);
    }

    /**
     * Public API for logging messages at different levels.
     * These methods are thread-safe and can be called from multiple producer threads.
     */
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

    /**
     * Logs an info-level message.
     * @param message the message to log
     */
    public void info(String message) { log(LogLevel.INFO, message); }
    /**
     * Logs a debug-level message.
     * @param message the message to log
     */
    public void debug(String message) { log(LogLevel.DEBUG, message); }
    /**
     * Logs a warning-level message.
     * @param message the message to log
     */
    public void warn(String message) { log(LogLevel.WARN, message); }
    /**
     * Logs an error-level message.
     * @param message the message to log
     */
    public void error(String message) { log(LogLevel.ERROR, message); }

    /**
     * Gracefully shuts down the logger, ensuring all queued log messages are processed before termination.
     * This method blocks until the worker thread has completed processing.
     */
    public void shutdown() throws InterruptedException {
        running = false;
        workerThread.join(); // Wait for worker thread to drain remaining logs
        System.out.println("AsyncLogger shut down successfully.");
    }

    /**
     * Main method demonstrating the usage of AsyncLogger with multiple concurrent producer threads.
     * Each thread generates log messages at different levels, simulating a real-world application scenario.
     */
    public static void main(String[] args) throws InterruptedException {
        // Initialize logger with a queue capacity of 50
        AsyncLogger logger = new AsyncLogger(50);

        // Create multiple concurrent application threads generating logs
        Thread[] appThreads = new Thread[5];
        // Each thread simulates a series of log messages at different levels
        for (int i = 0; i < appThreads.length; i++) {
            final int threadId = i + 1;
            appThreads[i] = new Thread(() -> {
                // Simulate logging activity with different log levels
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
        Thread.sleep(5000);
        // Gracefully shut down the logger, ensuring all messages are processed
        logger.shutdown();
    }
}
