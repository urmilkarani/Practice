# LongestSubstringWithoutDuplicates - Visual Documentation

## Problem Statement
Given a string `value`, find the length of the longest substring without repeating characters.

## Algorithm: Sliding Window with HashMap

### Core Idea
Use a sliding window [left, right] with a HashMap tracking the last index of each character. When a duplicate is found, move left to max(current left, duplicate index + 1).

### Visualization

#### Example: value = "zxyzxyz"

```
Initial: left=0, result=0, indices={}

right=0, char='z':
  indices: 'z' not found
  indices['z']=0
  result = max(0, 0-0+1) = 1

right=1, char='x':
  indices: 'x' not found
  indices['x']=1
  result = max(1, 1-0+1) = 2

right=2, char='y':
  indices: 'y' not found
  indices['y']=2
  result = max(2, 2-0+1) = 3

right=3, char='z':
  indices: 'z' found at 0
  left = max(0, 0+1) = 1
  indices['z']=3
  result = max(3, 3-1+1) = 3

right=4, char='x':
  indices: 'x' found at 1
  left = max(1, 1+1) = 2
  indices['x']=4
  result = max(3, 4-2+1) = 3

right=5, char='y':
  indices: 'y' found at 2
  left = max(2, 2+1) = 3
  indices['y']=5
  result = max(3, 5-3+1) = 3

right=6, char='z':
  indices: 'z' found at 3
  left = max(3, 3+1) = 4
  indices['z']=6
  result = max(3, 6-4+1) = 3

Final result = 3 (substring "xyz" or "yzx" or "zxy")
```

### Window Visualization

```
String:  z  x  y  z  x  y  z
Index:   0  1  2  3  4  5  6

Window progression:
[0,0] "z"           length=1
[0,1] "zx"          length=2
[0,2] "zxy"         length=3 ← max
[1,3] "xyz"         length=3 (duplicate 'z' at 0, left=1)
[2,4] "yzx"         length=3 (duplicate 'x' at 1, left=2)
[3,5] "zxy"         length=3 (duplicate 'y' at 2, left=3)
[4,6] "xyz"         length=3 (duplicate 'z' at 3, left=4)
```

### HashMap State

```
After each step:
right=0: {'z':0}
right=1: {'z':0, 'x':1}
right=2: {'z':0, 'x':1, 'y':2}
right=3: {'z':3, 'x':1, 'y':2}  ← 'z' updated, left=1
right=4: {'z':3, 'x':4, 'y':2}  ← 'x' updated, left=2
right=5: {'z':3, 'x':4, 'y':5}  ← 'y' updated, left=3
right=6: {'z':6, 'x':4, 'y':5}  ← 'z' updated, left=4
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| lengthOfLongestSubstring | O(n) | O(min(n, m)) |

Where m is the character set size (e.g., 128 for ASCII).

### Key Points
- Sliding window expands with right, contracts with left
- HashMap stores last seen index of each character
- left jumps to duplicate_index + 1 (not just left++)
- O(n) time - each character visited at most twice

---

## File Reference
- **Source**: `src/main/java/org/example/LongestSubstringWithoutDuplicates.java`
- **Package**: `org.example`
- **Test Case**: `lengthOfLongestSubstring("zxyzxyz")` → `3`