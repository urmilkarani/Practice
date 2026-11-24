package org.example.lowlevel;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A thread-safe LRU (Least Recently Used) cache implemented using a synchronized LinkedHashMap.
 * This cache evicts the least recently accessed entry when the specified capacity is exceeded.
 *
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class SynchronizedLinkedLRUCache<K, V> {
    private final Map<K, V> map;

    public SynchronizedLinkedLRUCache(int capacity) {
        this.map = new LinkedHashMap<K, V>(capacity, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > capacity;
            }
        };
    }

    public synchronized V get(K key) {
        return map.get(key);
    }

    public synchronized void put(K key, V value) {
        map.put(key, value);
    }

    public static void main(String[] args) {
        SynchronizedLinkedLRUCache<Integer, String> cache = new SynchronizedLinkedLRUCache<>(3);

        cache.put(1, "A");
        cache.put(2, "B");
        cache.put(3, "C");

        // Accessing key 1 moves it to the end of the access list (most recently used)
        cache.get(1);

        // This insertion exceeds capacity (3).
        // Key 2 is now the least recently used/eldest, so it gets removed.
        cache.put(4, "D");

        System.out.println(cache); // Output: {3=C, 1=A, 4=D}
    }
}

