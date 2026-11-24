# ThreeSum - Visual Documentation

## Problem Statement
Given an integer array `numbers`, return all unique triplets `[numbers[i], numbers[j], numbers[k]]` such that `i != j`, `i != k`, `j != k`, and `numbers[i] + numbers[j] + numbers[k] == 0`.

## Algorithm: Sort + Two Pointers

### Core Idea
1. Sort the array
2. For each element as the first element of triplet, use two pointers to find pairs that sum to its negative
3. Skip duplicates to avoid duplicate triplets

### Visualization

#### Example: numbers = [-1, 0, 1, 2, -1, -4]

```
Step 1: Sort
sorted = [-4, -1, -1, 0, 1, 2]

Step 2: Iterate with index as first element
index=0, value=-4:
  target = 4 (need two numbers summing to 4)
  left=1 (-1), right=5 (2): sum=1 < 4 → left++
  left=2 (-1), right=5 (2): sum=1 < 4 → left++
  left=3 (0), right=5 (2): sum=2 < 4 → left++
  left=4 (1), right=5 (2): sum=3 < 4 → left++
  left=5, right=5: stop

index=1, value=-1:
  target = 1
  left=2 (-1), right=5 (2): sum=1 == 1 → FOUND [-1, -1, 2]
  left=3 (0), right=4 (1): sum=1 == 1 → FOUND [-1, 0, 1]
  left=4, right=3: stop

index=2, value=-1: skip (duplicate of index=1)

index=3, value=0: break (value > 0, can't sum to 0 with positive numbers)
```

### Two Pointers Visualization

```
Sorted: [-4, -1, -1, 0, 1, 2]
         ↑  ↑           ↑
       idx left       right

For index=1 (value=-1), target=1:
left=2 (-1), right=5 (2): -1+2=1 ✓ → [-1, -1, 2]
left=3 (0), right=4 (1): 0+1=1 ✓ → [-1, 0, 1]
```

### Duplicate Skipping
```
if (index > 0 && numbers[index] == numbers[index - 1]) continue;

After finding triplet:
while (left < right && numbers[left] == numbers[left - 1]) left++;
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| threeSum | O(n²) | O(1) or O(n) for sorting |

### Key Points
- Sorting enables two-pointer technique
- Skip duplicates at both index level and pointer level
- Early break when first element > 0
- Result doesn't count as extra space

---

## File Reference
- **Source**: `src/main/java/org/example/ThreeSum.java`
- **Package**: `org.example`
- **Test Case**: `threeSum([-1,0,1,2,-1,-4])` → `[[-1,-1,2], [-1,0,1]]`