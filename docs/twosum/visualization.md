# TwoSum - Visual Documentation

## Problem Statement
Given an array of integers `numbers` and an integer `target`, return indices of the two numbers such that they add up to `target`.

## Algorithm: Hash Map (One Pass)

### Core Idea
Use a HashMap to store `number → index` mapping. For each number, check if its complement (`target - number`) exists in the map.

### Visualization

#### Initial State
```
numbers = [3, 4, 5, 6], target = 7
indices = {} (empty HashMap)
```

#### Step-by-Step Execution

```
index=0, number=3:
  complement = 7 - 3 = 4
  indices.containsKey(4)? NO
  indices.put(3, 0)
  indices = {3→0}

index=1, number=4:
  complement = 7 - 4 = 3
  indices.containsKey(3)? YES → return [0, 1]
  indices = {3→0, 4→1}
```

### Data Structure Visualization

```
HashMap: number → index
┌─────────────────────────────────────┐
│  Key (number)  │  Value (index)     │
├────────────────┼────────────────────┤
│       3        │        0           │
│       4        │        1           │
│       5        │        2           │
│       6        │        3           │
└────────────────┴────────────────────┘
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| twoSum    | O(n) | O(n)  |

### Key Points
- Single pass through array
- HashMap provides O(1) lookup for complement
- Returns first valid pair found

---

## File Reference
- **Source**: `src/main/java/org/example/TwoSum.java`
- **Package**: `org.example`
- **Test Case**: `twoSum([3,4,5,6], 7)` → `[0, 1]` (3+4=7)