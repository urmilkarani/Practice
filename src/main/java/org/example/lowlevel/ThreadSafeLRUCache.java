package org.example.lowlevel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
/**
 * Provides the thread safe lru cache implementation used to demonstrate low-level concurrency patterns.
 */

public class ThreadSafeLRUCache<K, V> {

    // Capacity of the cache
    private final int capacity;
    // Map to store key-node pairs for O(1) access
    private final Map<K, Node<K, V>> map;
    // Dummy head and tail nodes to facilitate easy addition and removal of nodes
    private final Node<K, V> head;
    private final Node<K, V> tail;
    // Lock to ensure thread safety
    private final Lock lock = new ReentrantLock();

    // Doubly linked list node structure
    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> prev;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    /***
     * Constructor to initialize the LRU Cache with a given capacity.
     *
     * @param capacity The maximum number of items the cache can hold.
     * @throws IllegalArgumentException if the capacity is less than or equal to 0.
     */
    public ThreadSafeLRUCache(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be greater than 0");
        }
        this.capacity = capacity;
        this.map = new HashMap<>();

        // Initialize dummy head and tail nodes to eliminate null checks
        head = new Node<>(null, null);
        tail = new Node<>(null, null);
        head.next = tail;
        tail.prev = head;
    }

    /**
     * Retrieves the value associated with the given key from the cache.
     * If the key exists, it moves the corresponding node to the head of the list
     * to mark it as recently used.
     *
     * @param key The key whose associated value is to be returned.
     * @return The value associated with the key, or null if the key does not exist in the cache.
     */
    public V get(K key) {
        lock.lock();
        try {
            Node<K, V> node = map.get(key);
            if (node == null) {
                return null;
            }
            // Move the accessed node to the head of the list to mark it as recently used
            moveToHead(node);
            return node.value;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Inserts a key-value pair into the cache. If the key already exists, it updates the value
     * and moves the corresponding node to the head of the list. If the cache exceeds its capacity,
     * it removes the least recently used (LRU) item from both the map and the linked list.
     *
     * @param key   The key to be inserted or updated.
     * @param value The value to be associated with the key.
     */
    public void put(K key, V value) {
        lock.lock();
        try {
            Node<K, V> node = map.get(key);
            if (node != null) {
                node.value = value;
                moveToHead(node);
            } else {
                if (map.size() >= capacity) {
                    Node<K, V> lru = removeTail();
                    if (lru != null) {
                        map.remove(lru.key);
                    }
                }
                Node<K, V> newNode = new Node<>(key, value);
                addToHead(newNode);
                map.put(key, newNode);
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Returns the current number of items in the cache.
     *
     * @return The size of the cache.
     */
    public int size() {
        lock.lock();
        try {
            return map.size();
        } finally {
            lock.unlock();
        }
    }

    // --- Helper Methods (Must be called within locked context) ---

    /**
     * Removes a node from the doubly linked list.
     *
     * @param node The node to be removed.
     */
    private void removeNode(Node<K, V> node) {
        node.prev.next = node.next;
        node.next.prev = node.prev;
    }

    /**
     * Adds a node to the head of the doubly linked list.
     *
     * @param node The node to be added.
     */
    private void addToHead(Node<K, V> node) {
        node.prev = head;
        node.next = head.next;
        head.next.prev = node;
        head.next = node;
    }

    /**
     * Moves a node to the head of the doubly linked list, marking it as recently used.
     *
     * @param node The node to be moved.
     */
    private void moveToHead(Node<K, V> node) {
        removeNode(node);
        addToHead(node);
    }

    /**
     * Removes the least recently used (LRU) node from the tail of the doubly linked list.
     *
     * @return The removed LRU node, or null if the cache is empty.
     */
    private Node<K, V> removeTail() {
        Node<K, V> lru = tail.prev;
        if (lru == head) {
            return null; // Cache is empty
        }
        removeNode(lru);
        return lru;
    }

    /**
     * The main method for testing the ThreadSafeLRUCache implementation.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {
        // Simulate a simple test of the ThreadSafeLRUCache with multiple threads to demonstrate thread safety and LRU eviction.
        ThreadSafeLRUCache<Integer, String> cache = new ThreadSafeLRUCache<>(3);

        Thread thread1 = new Thread(() -> {
            cache.put(1, "One");
            cache.put(2, "Two");
            System.out.println("Thread 1: " + cache.get(1)); // Should print "One"
            cache.put(3, "Three");
            cache.put(4, "Four"); // This should evict key 2 (LRU)
            System.out.println("Thread 1: " + cache.get(2)); // Should print "null" (evicted)
        });

        Thread thread2 = new Thread(() -> {
            cache.put(5, "Five");
            System.out.println("Thread 2: " + cache.get(3)); // Should print "Three"
            cache.put(6, "Six"); // This should evict key 1 (LRU)
            System.out.println("Thread 2: " + cache.get(1)); // Should print "null" (evicted)
        });

        Thread thread3 = new Thread(() -> {
            System.out.println("Thread 3: " + cache.get(4)); // Should print "Four"
            cache.put(7, "Seven"); // This should evict key 5 (LRU)
            System.out.println("Thread 3: " + cache.get(5)); // Should print "null" (evicted)
        });

        Thread thread4 = new Thread(() -> {
            System.out.println("Thread 4: " + cache.get(6)); // Should print "Six"
            cache.put(8, "Eight"); // This should evict key 3 (LRU)
            System.out.println("Thread 4: " + cache.get(3)); // Should print "null" (evicted)
        });
        thread1.start();
        thread2.start();
        thread3.start();
        thread4.start();
    }
}