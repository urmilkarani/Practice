package org.example.lowlevel;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

public class MemoryPool<T> {
    private final Object[] pool;
    private int pointer; // Points to the next available slot in the pool stack
    private final Lock lock = new ReentrantLock();

    @SuppressWarnings("unchecked")
    public MemoryPool(int capacity, Supplier<T> factory) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        if (factory == null) {
            throw new IllegalArgumentException("Factory cannot be null");
        }

        this.pool = new Object[capacity];
        this.pointer = capacity;

        // Pre-allocate all objects upfront during initialization
        for (int i = 0; i < capacity; i++) {
            pool[i] = factory.get();
        }
    }

    // --- Acquire Object ---
    @SuppressWarnings("unchecked")
    public T acquire() {
        lock.lock();
        try {
            if (pointer <= 0) {
                throw new IllegalStateException("Memory pool exhausted. No free objects available.");
            }
            // Pop an object from the pool stack
            return (T) pool[--pointer];
        } finally {
            lock.unlock();
        }
    }

    // --- Release Object ---
    public void release(T obj) {
        if (obj == null) {
            throw new IllegalArgumentException("Cannot release null object");
        }
        lock.lock();
        try {
            if (pointer >= pool.length) {
                throw new IllegalStateException("Memory pool overflow. Released more objects than capacity.");
            }
            // Push the object back onto the pool stack
            pool[pointer++] = obj;
        } finally {
            lock.unlock();
        }
    }

    // --- Utility Method ---
    public int availableCount() {
        lock.lock();
        try {
            return pointer;
        } finally {
            lock.unlock();
        }
    }

    // --- Main Method for Demonstration ---
    public static class DataPacket {
        private int id;
        private final byte[] payload;

        public DataPacket(int id) {
            this.id = id;
            this.payload = new byte[1024]; // Pre-allocated 1 KB fixed buffer
        }

        public void reset(int newId) {
            this.id = newId;
            // Clear payload or reset internal states if needed
        }

        public int getId() {
            return id;
        }
    }

    public static void main(String[] args) {
        // Create a memory pool with a capacity of 3 DataPackets
        MemoryPool<DataPacket> pool = new MemoryPool<>(3, () -> new DataPacket(0));

        System.out.println("Initial available objects: " + pool.availableCount());

        // Acquire objects from the pool
        DataPacket p1 = pool.acquire();
        p1.reset(101);
        DataPacket p2 = pool.acquire();
        p2.reset(102);

        System.out.println("\nAcquired 2 packets. Available: " + pool.availableCount());
        System.out.println("Packet 1 ID: " + p1.getId());
        System.out.println("Packet 2 ID: " + p2.getId());

        // Release an object back to the pool
        pool.release(p1);
        System.out.println("\nReleased Packet 1. Available: " + pool.availableCount());

        // Acquire again (recycles p1)
        DataPacket p3 = pool.acquire();
        p3.reset(103);
        System.out.println("Acquired Packet 3. ID: " + p3.getId() + ", Available: " + pool.availableCount());

        // Exhaust the pool completely
        DataPacket p4 = pool.acquire();
        p4.reset(104);
        DataPacket p5 = pool.acquire();
        p5.reset(105);

        System.out.println("\nPool exhausted. Available: " + pool.availableCount());

        try {
            pool.acquire(); // Should throw an exception
        } catch (IllegalStateException e) {
            System.out.println("Caught expected exception: " + e.getMessage());
        }

        // Release all items back
        pool.release(p2);
        pool.release(p3);
        pool.release(p4);
        pool.release(p5);
        System.out.println("\nReleased all packets. Available: " + pool.availableCount());
    }
}
