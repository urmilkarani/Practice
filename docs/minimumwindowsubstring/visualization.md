# MinimumWindowSubstring - Visual Documentation

## Problem Statement
Given strings `s` and `t`, find the minimum window in `s` which contains all characters of `t` (including duplicates).

## Algorithm: Sliding Window with Character Counting

### Core Idea
Use a sliding window with an array of size 128 (ASCII) to track required character counts. Expand right to include characters, contract left when all required characters are covered.

### Visualization

#### Example: s = "ADOBECODEBANC", t = "ABC"

```
Initial: arr[128] = {0}, counter=0, minLength=∞, result=""

Build required counts from t:
arr['A']=1, arr['B']=1, arr['C']=1

Window expansion (right pointer):
right=0 'A': arr['A']--=0 ≥0 → counter=1
right=1 'D': arr['D']--=-1 <0 → counter=1
right=2 'O': arr['O']--=-1 <0 → counter=1
right=3 'B': arr['B']--=0 ≥0 → counter=2
right=4 'E': arr['E']--=-1 <0 → counter=2
right=5 'C': arr['C']--=0 ≥0 → counter=3 == t.length!

Window contraction (left pointer):
left=0 'A': window="ADOBEC" (len=6), minLength=6, result="ADOBEC"
  arr['A']++=1 >0 → counter=2, left=1

Continue expanding right...
right=6 'O': arr['O']--=-2 <0
right=7 'D': arr['D']--=-2 <0
right=8 'E': arr['E']--=-2 <0
right=9 'B': arr['B']--=-1 <0
right=10 'A': arr['A']--=0 ≥0 → counter=3 == t.length!

Contract left:
left=1 'D': window="DOBECODEBA" (len=10) > minLength
  arr['D']++=-1 ≤0, left=2
left=2 'O': arr['O']++=-1 ≤0, left=3
left=3 'B': arr['B']++=0 ≤0, left=4
left=4 'E': arr['E']++=-1 ≤0, left=5
left=5 'C': arr['C']++=1 >0 → counter=2, left=6

Continue...
right=11 'N': arr['N']--=-1
right=12 'C': arr['C']--=0 ≥0 → counter=3 == t.length!

Contract left:
left=6 'O': window="ODEBANC" (len=7) > minLength
left=7 'D': arr['D']++=-1
left=8 'E': arr['E']++=-1
left=9 'B': arr['B']++=0
left=10 'A': arr['A']++=1 >0 → counter=2, left=11

Final result = "BANC" (length 4)
```

### Array State Visualization

```
arr[128] tracks: positive = needed, negative = extra, zero = satisfied

Initial from t="ABC":
arr['A']=1, arr['B']=1, arr['C']=1, others=0

During window "ADOBEC":
arr['A']=0, arr['B']=0, arr['C']=0, arr['D']=-1, arr['O']=-1, arr['E']=-1
counter=3 (all required satisfied)

After contracting 'A':
arr['A']=1, arr['B']=0, arr['C']=0, ...
counter=2 (need 'A' again)
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| minimunWindowSubstring | O(n + m) | O(1) |

Where n = s.length, m = t.length. Space O(1) for fixed 128 array.

### Key Points
- Fixed array for ASCII character counts
- counter tracks how many required characters are satisfied
- Expand right until counter == t.length
- Contract left to find minimum valid window
- Update result when smaller valid window found

---

## File Reference
- **Source**: `src/main/java/org/example/MinimumWindowSubstring.java`
- **Package**: `org.example`
- **Test Case**: `minimunWindowSubstring("ADOBECODEBANC", "ABC")` → `"BANC"`