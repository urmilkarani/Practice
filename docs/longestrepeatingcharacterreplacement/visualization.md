# LongestRepeatingCharacterReplacement - Visual Documentation

## Problem Statement
Given a string `value` and an integer `replacements`, find the length of the longest substring containing the same letter after performing at most `replacements` character changes.

## Algorithm: Sliding Window with Frequency Count

### Core Idea
Maintain a sliding window [left, right] with character frequencies. The window is valid if `(window_length - max_frequency) <= replacements`. Expand right, contract left when invalid.

### Visualization

#### Example: value = "XYYX", replacements = 2

```
Initial: left=0, result=0, maxFrequency=0, counts={}

right=0, char='X':
  counts['X']=1, maxFrequency=1
  window_len=1, 1-1=0 ≤ 2 ✓
  result = max(0, 1) = 1

right=1, char='Y':
  counts['Y']=1, maxFrequency=1
  window_len=2, 2-1=1 ≤ 2 ✓
  result = max(1, 2) = 2

right=2, char='Y':
  counts['Y']=2, maxFrequency=2
  window_len=3, 3-2=1 ≤ 2 ✓
  result = max(2, 3) = 3

right=3, char='X':
  counts['X']=2, maxFrequency=2
  window_len=4, 4-2=2 ≤ 2 ✓
  result = max(3, 4) = 4

Final result = 4 (entire string "XYYX" → "XXXX" or "YYYY" with 2 replacements)
```

### Window Visualization

```
String:  X  Y  Y  X
Index:   0  1  2  3

Window progression:
[0,0] "X"           counts={X:1}, maxFreq=1, replacements_needed=0
[0,1] "XY"          counts={X:1,Y:1}, maxFreq=1, replacements_needed=1
[0,2] "XYY"         counts={X:1,Y:2}, maxFreq=2, replacements_needed=1
[0,3] "XYYX"        counts={X:2,Y:2}, maxFreq=2, replacements_needed=2 ✓

All 4 chars can be made same with 2 replacements!
```

### Key Formula
```
replacements_needed = window_length - max_frequency_in_window

If replacements_needed > allowed_replacements:
  shrink window from left
```

### Why maxFrequency Doesn't Need to Decrease
```
When we shrink the window, maxFrequency might become stale (higher than actual).
But this is OK because:
- A stale maxFrequency only makes the condition MORE lenient
- We only care about finding the MAXIMUM valid window
- If a window was valid with a higher maxFrequency, it's still valid
- The result only increases, never decreases
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| characterReplacement | O(n) | O(1) |

Space is O(1) because at most 26 characters (uppercase English letters).

### Key Points
- Sliding window with frequency tracking
- Window valid if (length - maxFreq) ≤ k
- maxFrequency can be stale but doesn't affect correctness
- Expands right, contracts left only when necessary

---

## File Reference
- **Source**: `src/main/java/org/example/LongestRepeatingCharacterReplacement.java`
- **Package**: `org.example`
- **Test Case**: `characterReplacement("XYYX", 2)` → `4`