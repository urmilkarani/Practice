# SlidingWindowMaximumPQ - Visual Documentation

## Problem Statement
Given an array `nums` and an integer `k`, return the maximum values in each sliding window of size `k`.

## Algorithm: Max Heap (PriorityQueue)

### Core Idea
Use a max heap to store (value, index) pairs. For each window, remove elements that are outside the window (index <= i-k), then the heap top is the maximum.

### Visualization

#### Example: nums = [1, 3, -1, -3, 5, 3, 6, 7], k = 3

```
Heap stores [value, index], ordered by value (max at top)

i=0: heap=[[1,0]]                    (size < k-1, no output)
i=1: heap=[[3,1], [1,0]]             (size < k-1, no output)
i=2: heap=[[3,1], [1,0], [-1,2]]     i >= k-1 (2>=2)
  Remove expired: none (all indices > 2-3=-1)
  output[0] = heap.peek()[0] = 3

i=3: heap=[[3,1], [1,0], [-1,2], [-3,3]]
  Remove expired: index 0 <= 3-3=0? YES, remove [1,0]
  heap=[[3,1], [-1,2], [-3,3]]
  output[1] = 3

i=4: heap=[[5,4], [3,1], [-1,2], [-3,3]]
  Remove expired: index 1 <= 4-3=1? YES, remove [3,1]
  heap=[[5,4], [-1,2], [-3,3]]
  output[2] = 5

i=5: heap=[[5,4], [3,5], [-1,2], [-3,3]]
  Remove expired: index 2 <= 5-3=2? YES, remove [-1,2]
  heap=[[5,4], [3,5], [-3,3]]
  output[3] = 5

i=6: heap=[[6,6], [5,4], [3,5], [-3,3]]
  Remove expired: index 3 <= 6-3=3? YES, remove [-3,3]
  heap=[[6,6], [5,4], [3,5]]
  output[4] = 6

i=7: heap=[[7,7], [6,6], [3,5], [5,4]]
  Remove expired: index 4 <= 7-3=4? YES, remove [5,4]
  heap=[[7,7], [6,6], [3,5]]
  output[5] = 7

Result: [3, 3, 5, 5, 6, 7]
```

### Heap Visualization

```
At i=4 (window indices 2,3,4):
Heap before cleanup: [[5,4], [3,1], [-1,2], [-3,3]]
                      ↑
                    max=5

Remove expired (index <= 1): [3,1] removed
Heap after: [[5,4], [-1,2], [-3,3]]
             ↑
           max=5
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| slidingWindowMaximum | O(n log k) | O(k) |

### Key Points
- Max heap keeps largest element at top
- Store (value, index) to track window boundaries
- Lazy removal: only remove expired elements when they reach top
- Heap size at most k
- Simpler to implement than deque but slightly slower

---

## File Reference
- **Source**: `src/main/java/org/example/SlidingWindowMaximumPQ.java`
- **Package**: `org.example`
- **Test Case**: `slidingWindowMaximum([1,3,-1,-3,5,3,6,7], 3)` → `[3,3,5,5,6,7]`