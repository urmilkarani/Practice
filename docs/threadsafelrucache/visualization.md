# ThreadSafeLRUCache - Visual Documentation

## Overview

This document provides a visual explanation of the `ThreadSafeLRUCache` implementation - a thread-safe Least Recently Used (LRU) cache using a HashMap + Doubly Linked List with ReentrantLock for synchronization.

---

## Core Data Structures

### Class Structure

```
┌─────────────────────────────────────────────────────────────────┐
│                    ThreadSafeLRUCache<K, V>                     │
├─────────────────────────────────────────────────────────────────┤
│  private final int capacity;           // Max cache size        │
│  private final Map<K, Node<K,V>> map;  // Key → Node lookup     │
│  private final Node<K,V> head;         // Dummy head (most recent)│
│  private final Node<K,V> tail;         // Dummy tail (least recent)│
│  private final Lock lock;              // ReentrantLock          │
└─────────────────────────────────────────────────────────────────┘
```

### Node Structure (Doubly Linked List)

```
┌────────────────────────────────────┐
│           Node<K, V>               │
├────────────────────────────────────┤
│  K key;                            │
│  V value;                          │
│  Node<K,V> prev;  ◄── previous     │
│  Node<K,V> next;  ◄── next         │
└────────────────────────────────────┘
```

### Initial State (After Constructor)

```
capacity = 3

map: {} (empty)

Linked List:
[HEAD] ⇄ [TAIL]
  ↑        ↑
 dummy   dummy
```

---

## Operation Visualizations

### 1. put(1, "One"), put(2, "Two"), put(3, "Three")

```
map: {1→Node1, 2→Node2, 3→Node3}

Linked List (Most Recent at HEAD side):
[HEAD] ⇄ [3,"Three"] ⇄ [2,"Two"] ⇄ [1,"One"] ⇄ [TAIL]
  ↑                                            ↑
```

### 2. get(2) - Access Key 2 (Moves to Front)

```
BEFORE get(2):
[HEAD] ⇄ [3,"Three"] ⇄ [2,"Two"] ⇄ [1,"One"] ⇄ [TAIL]

AFTER get(2) - moveToHead(2):
[HEAD] ⇄ [2,"Two"] ⇄ [3,"Three"] ⇄ [1,"One"] ⇄ [TAIL]
         ↑
      Most Recent
```

### 3. put(4, "Four") - Evicts LRU (Key 1)

```
BEFORE put(4,"Four") - at capacity:
map: {1→Node1, 2→Node2, 3→Node3}
[HEAD] ⇄ [2,"Two"] ⇄ [3,"Three"] ⇄ [1,"One"] ⇄ [TAIL]
                                              ↑
                                           LRU (tail.prev)

AFTER put(4,"Four") - evicts key 1:
map: {2→Node2, 3→Node3, 4→Node4}
[HEAD] ⇄ [4,"Four"] ⇄ [2,"Two"] ⇄ [3,"Three"] ⇄ [TAIL]
         ↑
      New Most Recent
```

---

## Key Operations Flow

### get(key) - Thread Safe Read

```
┌────────────────────────────────────────────┐
│           lock.lock()                      │
├────────────────────────────────────────────┤
│  node = map.get(key)                       │
│  if node == null: return null              │
│  moveToHead(node)  // Mark as recently used│
│  return node.value                         │
├────────────────────────────────────────────┤
│           lock.unlock() (finally)          │
└────────────────────────────────────────────┘
```

### put(key, value) - Thread Safe Write

```
┌────────────────────────────────────────────┐
│           lock.lock()                      │
├────────────────────────────────────────────┤
│  node = map.get(key)                       │
│  if node exists:                           │
│     node.value = value                     │
│     moveToHead(node)                       │
│  else:                                     │
│     if map.size() >= capacity:             │
│        lru = removeTail()                  │
│        map.remove(lru.key)                 │
│     newNode = Node(key, value)             │
│     addToHead(newNode)                     │
│     map.put(key, newNode)                  │
├────────────────────────────────────────────┤
│           lock.unlock() (finally)          │
└────────────────────────────────────────────┘
```

---

## Helper Methods (Internal Linked List Operations)

### addToHead(node) - Insert at Front (Most Recent)

```
BEFORE:                    AFTER:
[HEAD] ⇄ [A] ⇄ [B]        [HEAD] ⇄ [NEW] ⇄ [A] ⇄ [B]
  ↑                         ↑
 
node.prev = head
node.next = head.next
head.next.prev = node
head.next = node
```

### removeNode(node) - Unlink from List

```
BEFORE:                    AFTER:
[A] ⇄ [NODE] ⇄ [B]        [A] ⇄ [B]
                            ↑
node.prev.next = node.next
node.next.prev = node.prev
```

### moveToHead(node) - Combination

```
removeNode(node)  // Unlink from current position
addToHead(node)   // Insert at front
```

### removeTail() - Remove LRU (Least Recent)

```
BEFORE:                              AFTER:
[HEAD] ⇄ [A] ⇄ [B] ⇄ [LRU] ⇄ [TAIL]  [HEAD] ⇄ [A] ⇄ [B] ⇄ [TAIL]
                                              ↑
                                           returns LRU node
```

---

## Thread Safety Visualization

```
┌─────────────────────────────────────────────────────────────┐
│                    ReentrantLock                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Thread 1: get(1)        Thread 2: put(4, "Four")          │
│       │                       │                              │
│       ▼                       ▼                              │
│  ┌─────────┐             ┌─────────┐                        │
│  │ lock()  │             │ lock()  │  (BLOCKS until        │
│  └────┬────┘             └────┬────┘   Thread 1 unlocks)   │
│       │                       │                              │
│       ▼                       ▼                              │
│  [Critical Section]    [Critical Section]                   │
│  - map.get(1)          - map.get(4)                         │
│  - moveToHead()        - removeTail()                       │
│  - return value        - addToHead()                        │
│       │                       │                              │
│       ▼                       ▼                              │
│  ┌─────────┐             ┌─────────┐                        │
│  │unlock() │             │unlock() │                        │
│  └─────────┘             └─────────┘                        │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## Time Complexity Summary

| Operation | Time | Space |
|-----------|------|-------|
| `get(key)` | O(1) | O(1) |
| `put(key, value)` | O(1) | O(1) |
| `size()` | O(1) | O(1) |

**Why O(1)?**
- HashMap gives O(1) key → node lookup
- Doubly linked list gives O(1) insertion/removal at head/tail
- All operations use only pointer manipulation

---

## Test Case Walkthrough (from main)

```java
ThreadSafeLRUCache<Integer, String> cache = new ThreadSafeLRUCache<>(3);
cache.put(1, "One");      // [1]
cache.put(2, "Two");      // [2, 1]
cache.put(3, "Three");    // [3, 2, 1] - at capacity
cache.size()              // 3

cache.get(2);             // [2, 3, 1] - 2 moved to front
cache.put(4, "Four");     // [4, 2, 3] - 1 evicted (LRU)

cache.get(1);             // null (evicted)
cache.get(3);             // "Three" - [3, 4, 2]
cache.get(4);             // "Four"  - [4, 3, 2]
```

---

## Key Design Points

1. **Dummy Head/Tail** - Eliminates null checks in linked list operations
2. **Single Lock** - Coarse-grained locking (simple but may limit concurrency)
3. **HashMap + Doubly Linked List** - Classic LRU pattern for O(1) operations
4. **Move-to-front on access** - Implements "recently used" semantics

---

## Potential Improvements

### ReadWriteLock for Better Concurrency

```java
private final ReadWriteLock lock = new ReentrantReadWriteLock();

// get() uses read lock (allows concurrent reads)
public V get(K key) {
    lock.readLock().lock();
    try { ... } finally { lock.readLock().unlock(); }
}

// put() uses write lock (exclusive)
public void put(K key, V value) {
    lock.writeLock().lock();
    try { ... } finally { lock.writeLock().unlock(); }
}
```

### StampedLock (Java 8+) - Optimistic Reads

```java
private final StampedLock lock = new StampedLock();

public V get(K key) {
    long stamp = lock.tryOptimisticRead();
    Node<K,V> node = map.get(key);
    if (!lock.validate(stamp)) {
        stamp = lock.readLock();
        try {
            node = map.get(key);
        } finally {
            lock.unlockRead(stamp);
        }
    }
    if (node != null) moveToHead(node);
    return node != null ? node.value : null;
}
```

---

## File Reference

- **Source**: `src/main/java/org/example/ThreadSafeLRUCache.java`
- **Package**: `org.example`
- **Dependencies**: `java.util.HashMap`, `java.util.Map`, `java.util.concurrent.locks.Lock`, `java.util.concurrent.locks.ReentrantLock`