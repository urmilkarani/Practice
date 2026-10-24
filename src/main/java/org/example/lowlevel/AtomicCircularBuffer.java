package org.example.lowlevel;

import java.util.concurrent.atomic.AtomicLong;

public class AtomicCircularBuffer<E> {
    private final Object[] buffer;
    private final int mask;
    private final AtomicLong head = new AtomicLong(0);
    private final AtomicLong tail = new AtomicLong(0);

    public AtomicCircularBuffer(int capacity) {
        // Capacity must be a power of 2 for fast bitwise masking
        if (capacity <= 0 || (capacity & (capacity - 1)) != 0) {
            throw new IllegalArgumentException("Capacity must be a power of 2 greater than 0");
        }
        this.buffer = new Object[capacity];
        this.mask = capacity - 1;
    }

    // --- Non-Blocking Offer (Producer) ---
    public boolean offer(E e) {
        if (e == null) {
            throw new IllegalArgumentException("Null elements are not supported");
        }

        long currentTail = tail.get();
        long currentHead = head.get();

        // Check if buffer is full without acquiring a lock
        if (currentTail - currentHead >= buffer.length) {
            return false; // Immediately returns false instead of putting thread to sleep
        }

        // Atomically claim the tail slot
        if (tail.compareAndSet(currentTail, currentTail + 1)) {
            int index = (int) (currentTail & mask);
            buffer[index] = e;
            return true;
        }

        return false; // CAS failed due to thread contention; caller can retry or yield
    }

    // --- Non-Blocking Poll (Consumer) ---
    @SuppressWarnings("unchecked")
    public E poll() {
        long currentHead = head.get();
        long currentTail = tail.get();

        // Check if buffer is empty without acquiring a lock
        if (currentHead >= currentTail) {
            return null; // Immediately returns null instead of blocking
        }

        // Atomically claim the head slot
        if (head.compareAndSet(currentHead, currentHead + 1)) {
            int index = (int) (currentHead & mask);
            E element = (E) buffer[index];
            buffer[index] = null; // Clear reference to help garbage collection
            return element;
        }

        return null; // CAS failed due to thread contention
    }

    // --- Utility Methods ---
    public int size() {
        long currentTail = tail.get();
        long currentHead = head.get();
        return (int) Math.max(0, currentTail - currentHead);
    }

    public boolean isEmpty() {
        return head.get() >= tail.get();
    }

    // --- Main Method for Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        // Capacity must be a power of 2 (e.g., 4)
        AtomicCircularBuffer<String> buffer = new AtomicCircularBuffer<>(4);

        // Producer thread
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 8; i++) {
                String item = "Data-" + i;
                // Spin/yield until the non-blocking offer succeeds
                while (!buffer.offer(item)) {
                    Thread.yield();
                }
                System.out.println("Produced: " + item);
            }
        }, "Producer");

        // Consumer thread
        Thread consumer = new Thread(() -> {
            int consumed = 0;
            while (consumed < 8) {
                String item = buffer.poll();
                if (item != null) {
                    System.out.println("--> Consumed: " + item);
                    consumed++;
                } else {
                    Thread.yield();
                }
            }
        }, "Consumer");

        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        System.out.println("\nAtomic circular buffer demonstration completed.");
    }
}
