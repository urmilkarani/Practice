# ValidAnagram - Visual Documentation

## Problem Statement
Given two strings `first` and `second`, return `true` if they are anagrams of each other (contain the same characters with the same frequencies).

## Algorithm: Character Counting Array

### Core Idea
Use a fixed-size array of 26 integers (for lowercase 'a'-'z') to count character frequencies. Increment for first string, decrement for second. If all counts are zero, they're anagrams.

### Visualization

#### Initial State
```
first = "racecar", second = "carrace"
counts = [0, 0, 0, ..., 0] (26 zeros)
```

#### Step-by-Step Execution

```
Processing first string "racecar":
  r: counts['r'-'a']++ → counts[17] = 1
  a: counts['a'-'a']++ → counts[0] = 1
  c: counts['c'-'a']++ → counts[2] = 1
  e: counts['e'-'a']++ → counts[4] = 1
  c: counts['c'-'a']++ → counts[2] = 2
  a: counts['a'-'a']++ → counts[0] = 2
  r: counts['r'-'a']++ → counts[17] = 2

Processing second string "carrace":
  c: counts['c'-'a']-- → counts[2] = 1
  a: counts['a'-'a']-- → counts[0] = 1
  r: counts['r'-'a']-- → counts[17] = 1
  r: counts['r'-'a']-- → counts[17] = 0
  a: counts['a'-'a']-- → counts[0] = 0
  c: counts['c'-'a']-- → counts[2] = 0
  e: counts['e'-'a']-- → counts[4] = 0

Final counts: all zeros → return true
```

### Array Visualization

```
counts array (indices 0-25 for 'a'-'z'):

Index:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
Char:  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s  t  u  v  w  x  y  z
After first:  2  0  2  0  1  0  0  0  0  0  0  0  0  0  0  0  0  2  0  0  0  0  0  0  0  0
After second: 0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0  0
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| isAnagram | O(n) | O(1)  |

### Key Points
- O(1) space (fixed 26-element array)
- Single pass through both strings
- Early exit if lengths differ
- Assumes lowercase English letters only

---

## File Reference
- **Source**: `src/main/java/org/example/ValidAnagram.java`
- **Package**: `org.example`
- **Test Case**: `isAnagram("racecar", "carrace")` → `true`