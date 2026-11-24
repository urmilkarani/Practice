package org.example.lowlevel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Provides the bounded blocking queue implementation used to demonstrate low-level concurrency patterns.
 */


public class BoundedBlockingQueue<E> {
    private final Object[] items;
    private final Lock lock = new ReentrantLock();

    // Condition to block producer threads when the queue is full
    private final Condition notFull = lock.newCondition();

    // Condition to block consumer threads when the queue is empty
    private final Condition notEmpty = lock.newCondition();

    private int head = 0;
    private int tail = 0;
    private int count = 0;

    public BoundedBlockingQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.items = new Object[capacity];
    }

    // --- Put Operation (Producer) ---
    public void put(E element) throws InterruptedException {
        if (element == null) {
            throw new IllegalArgumentException("Null elements are not supported");
        }

        lock.lock();
        try {
            // Use a while loop to guard against spurious wakeups
            while (count == items.length) {
                notFull.await();
                System.out.println("Producer waiting, queue is full.");
            }

            items[tail] = element;
            /*
             * Move the tail pointer forward in a circular manner.
             * If tail reaches the end of the array, it wraps around to 0.
             */
            tail = (tail + 1) % items.length;
            count++;

            // Signal waiting consumers that an item is now available
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    // --- Take Operation (Consumer) ---
    @SuppressWarnings("unchecked")
    public E take() throws InterruptedException {
        lock.lock();
        try {
            // Guard against spurious wakeups
            while (count == 0) {
                notEmpty.await();
                System.out.println("Consumer waiting, queue is empty.");
            }

            E element = (E) items[head];
            items[head] = null; // Clear reference to help garbage collection
            /*
             * Move the head pointer forward in a circular manner.
             * If head reaches the end of the array, it wraps around to 0.
             */
            head = (head + 1) % items.length;
            count--;

            // Signal waiting producers that space is now available
            notFull.signal();
            return element;
        } finally {
            lock.unlock();
        }
    }

    // --- Utility Methods ---
    public int size() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return count == 0;
        } finally {
            lock.unlock();
        }
    }

    static void main(String[] args) {
        BoundedBlockingQueue<Integer> queue = new BoundedBlockingQueue<>(5);

        // Producer thread
        Runnable producer = () -> {
            try {
                for (int i = 0; i < 50; i++) {
                    queue.put(i);
                    Thread.sleep(100); // Simulate some delay
                    System.out.println("Produced: " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        // Consumer thread
        Runnable consumer = () -> {
            try {
                for (int i = 0; i < 50; i++) {
                    int value = queue.take();
                    Thread.sleep(150); // Simulate some delay
                    System.out.println("Consumed: " + value);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Thread producerThread = new Thread(producer);
        Thread consumerThread = new Thread(consumer);

        producerThread.start();
        consumerThread.start();

        try {
            producerThread.join();
            consumerThread.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
