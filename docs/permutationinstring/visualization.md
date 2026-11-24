# PermutationInString - Visual Documentation

## Problem Statement
Given two strings `pattern` and `value`, return `true` if `value` contains a permutation of `pattern` (i.e., a substring of `value` that is an anagram of `pattern`).

## Algorithm: Sliding Window with Character Count Matching

### Core Idea
Use fixed-size sliding window of length `pattern.length()`. Track character counts in both pattern and current window. Maintain a `matches` counter for how many characters have equal counts.

### Visualization

#### Example: pattern = "abc", value = "lecabee"

```
patternCounts: [a:1, b:1, c:1, others:0]
windowCounts (first 3 chars "lec"): [l:1, e:1, c:1, others:0]

Initial matches calculation:
Compare all 26 letters:
a: 1 vs 0 → no match
b: 1 vs 0 → no match
c: 1 vs 1 → match!
... others 0 vs 0 → match
matches = 24 (24 letters have equal counts)

Sliding window:
right=3, left=0: add 'a', remove 'l'
  window: "eca" → counts: e:1, c:1, a:1
  matches = 26! → return true
```

### Step-by-Step

```
pattern = "abc" (length 3)
value = "lecabee" (length 7)

Initial window "lec" (indices 0-2):
patternCounts: a=1, b=1, c=1
windowCounts:  l=1, e=1, c=1
matches = 24 (only a,b differ)

Slide 1: remove 'l'(index 0), add 'a'(index 3)
window = "eca" (indices 1-3)
windowCounts: e=1, c=1, a=1
patternCounts: a=1, b=1, c=1
matches = 25 (only b differs)

Slide 2: remove 'e'(index 1), add 'b'(index 4)
window = "cab" (indices 2-4)
windowCounts: c=1, a=1, b=1
patternCounts: a=1, b=1, c=1
matches = 26! → PERMUTATION FOUND!
```

### Matches Update Logic

```
When adding character 'x':
  windowCounts[x]++
  if windowCounts[x] == patternCounts[x]: matches++
  else if windowCounts[x] == patternCounts[x] + 1: matches--

When removing character 'y':
  windowCounts[y]--
  if windowCounts[y] == patternCounts[y]: matches++
  else if windowCounts[y] == patternCounts[y] - 1: matches--
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| checkInclusion | O(n + m) | O(1) |

Where n = value.length, m = pattern.length. Space O(1) for fixed 26 arrays.

### Key Points
- Fixed window size = pattern.length()
- O(1) space with 26-element arrays
- `matches` tracks how many characters have equal counts
- Early exit when matches == 26
- Efficient sliding window updates

---

## File Reference
- **Source**: `src/main/java/org/example/PermutationInString.java`
- **Package**: `org.example`
- **Test Case**: `checkInclusion("abc", "lecabee")` → `true`