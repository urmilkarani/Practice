# LongestConsecutiveSequence - Visual Documentation

## Problem Statement
Given an unsorted array of integers `numbers`, find the length of the longest consecutive elements sequence.

## Algorithm: HashSet with Sequence Start Detection

### Core Idea
Put all numbers in a HashSet for O(1) lookup. For each number, only start counting a sequence if it's the beginning (number-1 not in set). Then count consecutive numbers forward.

### Visualization

#### Example: numbers = [2, 20, 4, 10, 3, 4, 5]

```
Step 1: Build HashSet
numberSet = {2, 20, 4, 10, 3, 5}  (duplicates removed)

Step 2: Check each number as potential sequence start
number=2:  1 not in set → START sequence
  count: 2,3,4,5 in set → length=4
  longest = 4

number=20: 19 not in set → START sequence
  count: 20 in set, 21 not → length=1
  longest = 4

number=4:  3 in set → NOT start, skip

number=10: 9 not in set → START sequence
  count: 10 in set, 11 not → length=1
  longest = 4

number=3:  2 in set → NOT start, skip

number=5:  4 in set → NOT start, skip

Final longest = 4 (sequence: 2,3,4,5)
```

### HashSet Visualization

```
numberSet = {2, 3, 4, 5, 10, 20}

Sequence detection:
  2 → 1 not in set ✓ START → 2,3,4,5 (len=4)
  3 → 2 in set ✗ skip
  4 → 3 in set ✗ skip
  5 → 4 in set ✗ skip
  10 → 9 not in set ✓ START → 10 (len=1)
  20 → 19 not in set ✓ START → 20 (len=1)
```

### Why Only Check Sequence Starts
```
If we check every number:
  2: counts 2,3,4,5
  3: counts 3,4,5
  4: counts 4,5
  5: counts 5
  ... redundant work!

By only starting at sequence beginnings:
  Each number visited at most twice (once as start check, once in sequence)
  Total O(n) time
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| longestConsecutive | O(n) | O(n) |

### Key Points
- HashSet for O(1) containment checks
- Only expand sequences from their minimum element
- Each element part of at most one sequence expansion
- Overall O(n) despite nested while loop

---

## File Reference
- **Source**: `src/main/java/org/example/LongestConsecutiveSequence.java`
- **Package**: `org.example`
- **Test Case**: `longestConsecutive([2,20,4,10,3,4,5])` → `4`