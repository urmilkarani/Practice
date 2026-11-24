# ValidPalindrome - Visual Documentation

## Problem Statement
Given a string `value`, return `true` if it is a palindrome after converting all uppercase letters to lowercase and removing all non-alphanumeric characters.

## Algorithm: Two Pointers with Character Filtering

### Core Idea
Use two pointers (left and right) moving towards center. Skip non-alphanumeric characters and compare lowercase versions.

### Visualization

#### Initial State
```
value = "Was it a car or a cat I saw?"
left = 0, right = 28
```

#### Step-by-Step Execution

```
Iteration 1:
  left=0 ('W'), right=28 ('?')
  Skip right (non-alphanumeric) → right=27 ('w')
  Compare: 'w' == 'w' ✓
  left=1, right=26

Iteration 2:
  left=1 ('a'), right=26 ('a')
  Compare: 'a' == 'a' ✓
  left=2, right=25

Iteration 3:
  left=2 ('s'), right=25 ('s')
  Compare: 's' == 's' ✓
  left=3, right=24

... continues until left >= right ...
```

### Pointer Movement Visualization

```
Original: "Was it a car or a cat I saw?"
Indices:  01234567890123456789012345678

Filtered (alphanumeric, lowercase):
"wasitacaroracatisaw"
  ↑                   ↑
 left               right
 
Step 1: compare 'w' and 'w' ✓
Step 2: compare 'a' and 'a' ✓
Step 3: compare 's' and 's' ✓
...
All match → return true
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| isPalindrome | O(n) | O(1) |

### Key Points
- Two pointers from both ends
- Skip non-alphanumeric with `Character.isLetterOrDigit()`
- Case-insensitive comparison with `Character.toLowerCase()`
- O(1) space - no extra storage

---

## File Reference
- **Source**: `src/main/java/org/example/ValidPalindrome.java`
- **Package**: `org.example`
- **Test Case**: `isPalindrome("Was it a car or a cat I saw?")` → `true`