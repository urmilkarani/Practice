# AnagramGroups - Visual Documentation

## Problem Statement
Given an array of strings `words`, group the anagrams together. An anagram is a word formed by rearranging the letters of another word.

## Algorithm: Character Count Key

### Core Idea
Use a fixed-size array of 26 integers (for lowercase 'a'-'z') to count character frequencies for each word. Convert the count array to a string key (e.g., "[1,0,2,0,1,...]") and use it as a HashMap key to group words with identical character counts.

### Visualization

#### Example: words = ["eat", "tea", "tan", "ate", "nat", "bat"]

```
Initial State:
groups = {} (empty HashMap)

Processing "eat":
  counts = [1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0]
  key = "[1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0]"
  groups[key] = ["eat"]

Processing "tea":
  counts = [1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0]
  key = "[1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0]"
  groups[key] = ["eat", "tea"]

Processing "tan":
  counts = [1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0]
  key = "[1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0]"
  groups[key] = ["tan"]

Processing "ate":
  counts = [1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0]
  key = "[1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0]"
  groups[key] = ["eat", "tea", "ate"]

Processing "nat":
  counts = [1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0]
  key = "[1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0]"
  groups[key] = ["tan", "nat"]

Processing "bat":
  counts = [1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0]
  key = "[1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0]"
  groups[key] = ["bat"]

Final groups:
[
  ["eat", "tea", "ate"],
  ["tan", "nat"],
  ["bat"]
]
```

### Character Count Array Visualization

```
For word "eat":
Index:  0  1  2  3  4  5  6  7  8  9 10 11 12 13 14 15 16 17 18 19 20 21 22 23 24 25
Char:  a  b  c  d  e  f  g  h  i  j  k  l  m  n  o  p  q  r  s  t  u  v  w  x  y  z
Count:  1  0  0  0  1  0  0  0  0  0  0  0  0  0  0  0  0  0  0  1  0  0  0  0  0  0
        ↑                    ↑                    ↑
        a                    e                    t
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| groupAnagrams | O(n * k) | O(n * k) |

Where n = number of words, k = max length of a word.

### Key Points
- O(1) space for character count array (fixed 26 elements)
- HashMap key is the string representation of the count array
- All anagrams produce identical count arrays
- Order of groups and words within groups doesn't matter

---

## File Reference
- **Source**: `src/main/java/org/example/AnagramGroups.java`
- **Package**: `org.example`
- **Test Case**: `groupAnagrams(["eat","tea","tan","ate","nat","bat"])` → `[["eat","tea","ate"],["tan","nat"],["bat"]]`