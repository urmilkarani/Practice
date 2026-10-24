package org.example.lowlevel;

import java.util.Objects;

public class MyHashMap<K, V> {

    // Node structure for separate chaining (linked list nodes)
    private static class Entry<K, V> {
        K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private static final int DEFAULT_CAPACITY = 16;
    private static final float LOAD_FACTOR = 0.75f;

    private Entry<K, V>[] buckets;
    private int size;
    private int capacity;

    @SuppressWarnings("unchecked")
    public MyHashMap() {
        this.capacity = DEFAULT_CAPACITY;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    @SuppressWarnings("unchecked")
    public MyHashMap(int capacity) {
        this.capacity = capacity;
        this.buckets = new Entry[capacity];
        this.size = 0;
    }

    // --- Core Hash Function ---
    private int getBucketIndex(K key) {
        if (key == null) {
            return 0; // Handle null keys by mapping them to index 0
        }
        int hash = key.hashCode();
        // Spread higher bits to lower bits to reduce collisions
        hash ^= (hash >>> 16);
        return Math.abs(hash) % capacity;
    }

    // --- Put Operation ---
    public void put(K key, V value) {
        int index = getBucketIndex(key);
        Entry<K, V> head = buckets[index];

        // Check if key already exists; if so, update its value
        Entry<K, V> current = head;
        while (current != null) {
            if (Objects.equals(current.key, key)) {
                current.value = value;
                return;
            }
            current = current.next;
        }

        // Insert new entry at the head of the bucket's linked list
        Entry<K, V> newEntry = new Entry<>(key, value);
        newEntry.next = head;
        buckets[index] = newEntry;
        size++;

        // Check load factor and trigger resizing if threshold is exceeded
        if ((float) size / capacity >= LOAD_FACTOR) {
            resize();
        }
    }

    // --- Get Operation ---
    public V get(K key) {
        int index = getBucketIndex(key);
        Entry<K, V> current = buckets[index];

        while (current != null) {
            if (Objects.equals(current.key, key)) {
                return current.value;
            }
            current = current.next;
        }
        return null; // Key not found
    }

    // --- Remove Operation ---
    public V remove(K key) {
        int index = getBucketIndex(key);
        Entry<K, V> current = buckets[index];
        Entry<K, V> prev = null;

        while (current != null) {
            if (Objects.equals(current.key, key)) {
                if (prev == null) {
                    buckets[index] = current.next; // Remove head node
                } else {
                    prev.next = current.next; // Bypass current node
                }
                size--;
                return current.value;
            }
            prev = current;
            current = current.next;
        }
        return null; // Key not found
    }

    // --- Utility Methods ---
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    // --- Dynamic Resizing (Rehashing) ---
    @SuppressWarnings("unchecked")
    private void resize() {
        capacity *= 2;
        Entry<K, V>[] oldBuckets = buckets;
        buckets = new Entry[capacity];
        size = 0; // Reset size as elements will be re-inserted

        for (Entry<K, V> head : oldBuckets) {
            Entry<K, V> current = head;
            while (current != null) {
                put(current.key, current.value);
                current = current.next;
            }
        }
    }

    static void main(String[] args) {
        MyHashMap<String, Integer> map = new MyHashMap<>();
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);


        System.out.println("Value for 'two': " + map.get("two")); // Output: 2
        System.out.println("Size: " + map.size()); // Output: 3


        map.remove("two");
        System.out.println("Value for 'two' after removal: " + map.get("two")); // Output: null
        System.out.println("Size after removal: " + map.size()); // Output: 2

        map.put("four", 4);
        map.put("five", 5);
        map.put("six", 6);
        map.put("seven", 7);
        map.put("eight", 8);
        map.put("nine", 9);
        map.put("ten", 10);
        map.put("eleven", 11);
        map.put("twelve", 12);
        map.put("thirteen", 13);
        map.put("fourteen", 14);
        map.put("fifteen", 15);
        map.put("sixteen", 16);
        map.put("seventeen", 17);
        map.put("eighteen", 18);
        map.put("nineteen", 19);
        map.put("twenty", 20);
        System.out.println("Size after adding more elements: " + map.size()); // Output: 20
        System.out.println("Capacity: " + map.capacity); // Output: 32
    }
}
