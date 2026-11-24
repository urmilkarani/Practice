# TwoSumII - Visual Documentation

## Problem Statement
Given a **sorted** array of integers `sortedNumbers` and an integer `target`, return indices (1-indexed) of the two numbers such that they add up to `target`.

## Algorithm: Two Pointers

### Core Idea
Since the array is sorted, use two pointers - one at the start (left) and one at the end (right). Adjust pointers based on sum comparison with target.

### Visualization

#### Initial State
```
sortedNumbers = [1, 2, 3, 4], target = 3
left = 0, right = 3
```

#### Step-by-Step Execution

```
Iteration 1:
  left=0 (value=1), right=3 (value=4)
  sum = 1 + 4 = 5
  sum > target (5 > 3) → right--
  
Iteration 2:
  left=0 (value=1), right=2 (value=3)
  sum = 1 + 3 = 4
  sum > target (4 > 3) → right--
  
Iteration 3:
  left=0 (value=1), right=1 (value=2)
  sum = 1 + 2 = 3
  sum == target → return [left+1, right+1] = [1, 2]
```

### Pointer Movement Visualization

```
Array: [1, 2, 3, 4]
        ↑           ↑
      left        right
      
Step 1: sum=5 > 3, move right left
Array: [1, 2, 3, 4]
        ↑       ↑
      left    right
      
Step 2: sum=4 > 3, move right left
Array: [1, 2, 3, 4]
        ↑   ↑
      left right
      
Step 3: sum=3 == 3, FOUND!
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| twoSum    | O(n) | O(1)  |

### Key Points
- Array must be sorted
- Two pointers converge from both ends
- O(1) space - no extra data structures
- Returns 1-indexed positions

---

## File Reference
- **Source**: `src/main/java/org/example/TwoSumII.java`
- **Package**: `org.example`
- **Test Case**: `twoSum([1,2,3,4], 3)` → `[1, 2]` (1+2=3)