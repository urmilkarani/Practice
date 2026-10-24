package org.example.lowlevel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomReaderWriterLock {
    private int readingThreads = 0;
    private int writingThreads = 0;
    private int writeRequests = 0; // Tracks waiting writers to prevent writer starvation

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    // --- Acquire Read Lock ---
    public void lockRead() throws InterruptedException {
        lock.lock();
        try {
            // Wait if there is an active writer or pending writer requests (fairness/starvation prevention)
            while (writingThreads > 0 || writeRequests > 0) {
                condition.await();
            }
            readingThreads++;
        } finally {
            lock.unlock();
        }
    }

    // --- Release Read Lock ---
    public void unlockRead() {
        lock.lock();
        try {
            readingThreads--;
            if (readingThreads == 0) {
                condition.signalAll(); // Wake up waiting writers/readers
            }
        } finally {
            lock.unlock();
        }
    }

    // --- Acquire Write Lock ---
    public void lockWrite() throws InterruptedException {
        lock.lock();
        try {
            writeRequests++;
            while (readingThreads > 0 || writingThreads > 0) {
                condition.await();
            }
            writeRequests--;
            writingThreads++;
        } finally {
            lock.unlock();
        }
    }

    // --- Release Write Lock ---
    public void unlockWrite() {
        lock.lock();
        try {
            writingThreads--;
            condition.signalAll(); // Wake up all waiting threads
        } finally {
            lock.unlock();
        }
    }

    // --- Demonstration Main Method ---
    public static class SharedResource {
        private String data = "Initial Database State";
        private final CustomReaderWriterLock rwLock;

        public SharedResource(CustomReaderWriterLock rwLock) {
            this.rwLock = rwLock;
        }

        public void read() throws InterruptedException {
            rwLock.lockRead();
            try {
                System.out.println(Thread.currentThread().getName() + " STARTED reading. Data: " + data);
                Thread.sleep(100); // Simulate read latency
                System.out.println(Thread.currentThread().getName() + " FINISHED reading.");
            } finally {
                rwLock.unlockRead();
            }
        }

        public void write(String newData) throws InterruptedException {
            rwLock.lockWrite();
            try {
                System.out.println("--> " + Thread.currentThread().getName() + " STARTED writing.");
                this.data = newData;
                Thread.sleep(150); // Simulate write latency
                System.out.println("--> " + Thread.currentThread().getName() + " FINISHED writing. New data: " + data);
            } finally {
                rwLock.unlockWrite();
            }
        }
    }

    public static void main(String[] args) throws InterruptedException {
        CustomReaderWriterLock lock = new CustomReaderWriterLock();
        SharedResource resource = new SharedResource(lock);

        // Create multiple reader threads
        Thread[] readers = new Thread[4];
        for (int i = 0; i < readers.length; i++) {
            readers[i] = new Thread(() -> {
                try {
                    resource.read();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Reader-" + (i + 1));
        }

        // Create multiple writer threads
        Thread[] writers = new Thread[2];
        for (int i = 0; i < writers.length; i++) {
            final int writerId = i + 1;
            writers[i] = new Thread(() -> {
                try {
                    resource.write("Updated-Data-v" + writerId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Writer-" + writerId);
        }

        // Start readers and writers concurrently
        for (Thread r : readers) r.start();
        Thread.sleep(20); // Small stagger to let readers enqueue first
        for (Thread w : writers) w.start();

        // Join threads to ensure execution completes before exit
        for (Thread r : readers) r.join();
        for (Thread w : writers) w.join();

        System.out.println("\nAll read and write operations completed successfully.");
    }
}
