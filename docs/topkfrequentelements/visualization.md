# TopKFrequentElements - Visual Documentation

## Problem Statement
Given an integer array `numbers` and an integer `k`, return the `k` most frequent elements.

## Algorithm: HashMap + Min Heap (PriorityQueue)

### Core Idea
1. Count frequencies using HashMap
2. Use a min-heap of size k to keep track of top k frequent elements
3. When heap exceeds size k, remove the least frequent (root of min-heap)

### Visualization

#### Example: numbers = [1, 2, 2, 3, 3, 3], k = 2

```
Step 1: Count Frequencies
frequencies = {1→1, 2→2, 3→3}

Step 2: Build Min Heap (size k=2)
Heap stores [frequency, number] pairs, ordered by frequency (min at top)

Insert [1, 1]: heap = [[1,1]]           size=1 ≤ 2
Insert [2, 2]: heap = [[1,1], [2,2]]   size=2 ≤ 2
Insert [3, 3]: heap = [[1,1], [2,2], [3,3]] size=3 > 2 → poll min
  After poll: heap = [[2,2], [3,3]]

Step 3: Extract Results
Poll all: [2,2] → 2, [3,3] → 3
Result: [2, 3] (order may vary)
```

### Heap Visualization

```
Min Heap (ordered by frequency, smallest at root):

After inserting [1,1], [2,2], [3,3]:
       [1,1]          ← root (min frequency)
      /    \
  [2,2]    [3,3]

After polling (size > k=2):
       [2,2]          ← new root
      /    
  [3,3]    

Final heap contains top 2 frequent elements
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| topKFrequent | O(n log k) | O(n) |

### Key Points
- HashMap for O(n) frequency counting
- Min-heap maintains top k elements efficiently
- Heap size never exceeds k+1
- Alternative: Bucket sort for O(n) time

---

## File Reference
- **Source**: `src/main/java/org/example/TopKFrequentElements.java`
- **Package**: `org.example`
- **Test Case**: `topKFrequent([1,2,2,3,3,3], 2)` → `[2, 3]` (or `[3, 2]`)