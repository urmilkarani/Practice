# ContainsDuplicate - Visual Documentation

## Problem Statement
Given an integer array `numbers`, return `true` if any value appears at least twice in the array, and return `false` if every element is distinct.

## Algorithm: HashSet

### Core Idea
Use a HashSet to track seen numbers. If we encounter a number already in the set, we found a duplicate.

### Visualization

#### Initial State
```
numbers = [1, 2, 3, 3]
seen = {} (empty HashSet)
```

#### Step-by-Step Execution

```
number=1: seen.add(1) → true (added) → seen={1}
number=2: seen.add(2) → true (added) → seen={1,2}
number=3: seen.add(3) → true (added) → seen={1,2,3}
number=3: seen.add(3) → false (already exists) → return true
```

### HashSet Visualization

```
HashSet operations:
┌─────────────────────────────────────┐
│  Operation    │  Result  │  Set     │
├───────────────┼──────────┼──────────┤
│  add(1)       │  true    │  {1}     │
│  add(2)       │  true    │  {1,2}   │
│  add(3)       │  true    │  {1,2,3} │
│  add(3)       │  false   │  {1,2,3} │ ← DUPLICATE!
└───────────────┴──────────┴──────────┘
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| hasDuplicate | O(n) | O(n) |

### Key Points
- HashSet.add() returns false if element already exists
- Early exit on first duplicate
- Single pass through array

---

## File Reference
- **Source**: `src/main/java/org/example/ContainsDuplicate.java`
- **Package**: `org.example`
- **Test Case**: `hasDuplicate([1,2,3,3])` → `true`