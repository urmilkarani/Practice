# SlidingWindowMaximum - Visual Documentation

## Problem Statement
Given an array `nums` and an integer `k`, return the maximum values in each sliding window of size `k`.

## Algorithm: Dynamic Programming (Left/Right Arrays)

### Core Idea
Precompute maximums from left-to-right and right-to-left in blocks of size k. For each window, the maximum is max(left[i+k-1], right[i]).

### Visualization

#### Example: nums = [1, 3, -1, -3, 5, 3, 6, 7], k = 3

```
n = 8, k = 3
Number of windows = n - k + 1 = 6

Step 1: Build left array (max from left within each block)
left[0] = nums[0] = 1
left[1] = max(left[0], nums[1]) = max(1, 3) = 3
left[2] = max(left[1], nums[2]) = max(3, -1) = 3  ← block boundary (i%k==0? no)
left[3] = nums[3] = -3  ← new block (3%3==0)
left[4] = max(left[3], nums[4]) = max(-3, 5) = 5
left[5] = max(left[4], nums[5]) = max(5, 3) = 5
left[6] = nums[6] = 6  ← new block (6%3==0)
left[7] = max(left[6], nums[7]) = max(6, 7) = 7

left = [1, 3, 3, -3, 5, 5, 6, 7]

Step 2: Build right array (max from right within each block)
right[7] = nums[7] = 7
right[6] = max(right[7], nums[6]) = max(7, 6) = 7
right[5] = max(right[6], nums[5]) = max(7, 3) = 7  ← block boundary
right[4] = nums[4] = 5  ← new block
right[3] = max(right[4], nums[3]) = max(5, -3) = 5
right[2] = max(right[3], nums[2]) = max(5, -1) = 5
right[1] = nums[1] = 3  ← new block
right[0] = max(right[1], nums[0]) = max(3, 1) = 3

right = [3, 3, 5, 5, 5, 7, 7, 7]

Step 3: Compute result for each window
Window 0 (indices 0-2): max(left[2], right[0]) = max(3, 3) = 3
Window 1 (indices 1-3): max(left[3], right[1]) = max(-3, 3) = 3
Window 2 (indices 2-4): max(left[4], right[2]) = max(5, 5) = 5
Window 3 (indices 3-5): max(left[5], right[3]) = max(5, 5) = 5
Window 4 (indices 4-6): max(left[6], right[4]) = max(6, 5) = 6
Window 5 (indices 5-7): max(left[7], right[5]) = max(7, 7) = 7

Result: [3, 3, 5, 5, 6, 7]
```

### Array Visualization

```
nums:    [1,  3, -1, -3,  5,  3,  6,  7]
Index:    0   1   2   3   4   5   6   7

Blocks of k=3:
Block 0: indices 0,1,2
Block 1: indices 3,4,5
Block 2: indices 6,7

left (max from left in block):
[1, 3, 3, -3, 5, 5, 6, 7]
        ↑       ↑       ↑
      block    block   block
      end      end     end

right (max from right in block):
[3, 3, 5, 5, 5, 7, 7, 7]
  ↑       ↑       ↑
block    block   block
start    start   start

Window i uses: left[i+k-1] (max from left to end of window)
               right[i]     (max from start of window to right)
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| slidingWindowMaximum | O(n) | O(n) |

### Key Points
- O(n) time with two passes + result computation
- left[i] = max from block start to i
- right[i] = max from i to block end
- Window max = max(left[end], right[start])
- No deque needed, simpler than monotonic queue

---

## File Reference
- **Source**: `src/main/java/org/example/SlidingWindowMaximum.java`
- **Package**: `org.example`
- **Test Case**: `slidingWindowMaximum([1,3,-1,-3,5,3,6,7], 3)` → `[3,3,5,5,6,7]`