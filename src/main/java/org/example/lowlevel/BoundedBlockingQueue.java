package org.example.lowlevel;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
            }

            items[tail] = element;
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
            }

            E element = (E) items[head];
            items[head] = null; // Clear reference to help garbage collection
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

    static void main() {
        BoundedBlockingQueue<Integer> queue = new BoundedBlockingQueue<>(5);

        // Producer thread
        Thread producer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    queue.put(i);
                    System.out.println("Produced: " + i);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Consumer thread
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 0; i < 10; i++) {
                    int value = queue.take();
                    System.out.println("Consumed: " + value);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
