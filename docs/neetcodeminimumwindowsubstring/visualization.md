# NeetCodeMinimumWindowSubstring - Visual Documentation

## Problem Statement
Given strings `source` and `target`, find the minimum window in `source` which contains all characters of `target` (including duplicates).

## Algorithm: Sliding Window with Character Counting (Optimized)

### Core Idea
Similar to MinimumWindowSubstring but with cleaner implementation. Uses a single array for required counts and tracks matched characters.

### Visualization

#### Example: source = "OUZODYXAZV", target = "XYZ"

```
Initial: required[128] = {0}, matched=0, minLength=∞, result=""

Build required from target:
required['X']=1, required['Y']=1, required['Z']=1

Window expansion:
right=0 'O': required['O']--=-1 <0 → matched=0
right=1 'U': required['U']--=-1 <0 → matched=0
right=2 'Z': required['Z']--=0 ≥0 → matched=1
right=3 'O': required['O']--=-2 <0 → matched=1
right=4 'D': required['D']--=-1 <0 → matched=1
right=5 'Y': required['Y']--=0 ≥0 → matched=2
right=6 'X': required['X']--=0 ≥0 → matched=3 == target.length!

Window contraction:
left=0 'O': window="OUZODYX" (len=7), minLength=7, result="OUZODYX"
  required['O']++=-1 ≤0 → matched=3, left=1
left=1 'U': required['U']++=-1 ≤0 → matched=3, left=2
left=2 'Z': required['Z']++=1 >0 → matched=2, left=3

Continue expanding...
right=7 'A': required['A']--=-1
right=8 'Z': required['Z']--=0 ≥0 → matched=3 == target.length!

Contract left:
left=3 'O': window="ODYXAZ" (len=6) < minLength=7
  minLength=6, result="ODYXAZ"
  required['O']++=-1 ≤0, left=4
left=4 'D': required['D']++=-1 ≤0, left=5
left=5 'Y': required['Y']++=1 >0 → matched=2, left=6

Final result = "ODYXAZ" (length 6)
```

### Key Differences from MinimumWindowSubstring
```
1. Uses matched counter instead of checking all required
2. Cleaner contraction logic
3. Same time/space complexity
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| minWindow | O(n + m) | O(1) |

### Key Points
- Same sliding window approach
- `matched` increments only when required count goes from positive to zero
- `matched` decrements only when required count goes from zero to positive
- More concise implementation

---

## File Reference
- **Source**: `src/main/java/org/example/NeetCodeMinimumWindowSubstring.java`
- **Package**: `org.example`
- **Test Case**: `minWindow("OUZODYXAZV", "XYZ")` → `"ODYXAZ"`