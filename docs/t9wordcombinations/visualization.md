# T9WordCombinations - Visual Documentation

## Problem Statement
Given a sequence of T9 keypad digits and a dictionary of valid words, find all possible combinations of valid words that match the digit sequence exactly.

## Algorithm: HashMap + Backtracking

### Core Idea
1. Preprocess valid words into a HashMap mapping digit sequences to lists of words
2. Use backtracking to try all valid word prefixes at each position
3. Recursively explore remaining digits after each valid word match

### T9 Keypad Mapping
```
2: abc    3: def    4: ghi    5: jkl    6: mno
7: pqrs   8: tuv    9: wxyz
```

### Visualization

#### Example: digits = [7,6,6,3,8,4,6,3], validWords = ["some", "time", "rome", "sometime", "so", "me"]

```
Step 1: Build digitToWords Map
--------------------------------
"some"    → "7663"  → digitToWords["7663"] = ["some"]
"time"    → "8463"  → digitToWords["8463"] = ["time"]
"rome"    → "7663"  → digitToWords["7663"] = ["some", "rome"]
"sometime"→ "76638463" → digitToWords["76638463"] = ["sometime"]
"so"      → "76"    → digitToWords["76"] = ["so"]
"me"      → "63"    → digitToWords["63"] = ["me"]

Map:
"76"       → ["so"]
"63"       → ["me"]
"7663"     → ["some", "rome"]
"8463"     → ["time"]
"76638463" → ["sometime"]
```

```
Step 2: Backtracking on targetDigits = "76638463"
--------------------------------------------------
backtrack("76638463", 0, [], [])

start=0:
  end=1: sub="7"     → not in map
  end=2: sub="76"    → in map! words=["so"]
    → path=["so"]
    → backtrack("76638463", 2, ["so"], [])
    
    start=2:
      end=3: sub="6"     → not in map
      end=4: sub="63"    → in map! words=["me"]
        → path=["so", "me"]
        → backtrack("76638463", 4, ["so", "me"], [])
        
        start=4:
          end=5: sub="8"     → not in map
          end=6: sub="84"    → not in map
          end=7: sub="846"   → not in map
          end=8: sub="8463"  → in map! words=["time"]
            → path=["so", "me", "time"]
            → backtrack("76638463", 8, ["so", "me", "time"], [])
            → start=8 == len → FOUND: ["so", "me", "time"]
          (backtrack)
        (backtrack)
      end=5: sub="638"   → not in map
      ... (no more matches)
    (backtrack)
  
  end=3: sub="766"   → not in map
  end=4: sub="7663"  → in map! words=["some", "rome"]
    → path=["some"]
    → backtrack("76638463", 4, ["some"], [])
    
    start=4:
      end=5: sub="8"     → not in map
      end=6: sub="84"    → not in map
      end=7: sub="846"   → not in map
      end=8: sub="8463"  → in map! words=["time"]
        → path=["some", "time"]
        → backtrack("76638463", 8, ["some", "time"], [])
        → start=8 == len → FOUND: ["some", "time"]
      (backtrack)
    (backtrack)
    
    → path=["rome"]
    → backtrack("76638463", 4, ["rome"], [])
    ... same as above → FOUND: ["rome", "time"]
    (backtrack)
  
  end=5: sub="76638" → not in map
  end=6: sub="766384" → not in map
  end=7: sub="7663846" → not in map
  end=8: sub="76638463" → in map! words=["sometime"]
    → path=["sometime"]
    → backtrack("76638463", 8, ["sometime"], [])
    → start=8 == len → FOUND: ["sometime"]
    (backtrack)

Final Combinations:
[
  ["so", "me", "time"],
  ["some", "time"],
  ["rome", "time"],
  ["sometime"]
]
```

### Visual Flow Diagram

```
Target: 7 6 6 3 8 4 6 3
         │ │ │ │ │ │ │ │
         ▼ ▼ ▼ ▼ ▼ ▼ ▼ ▼
        ┌─────────────────┐
        │  backtrack(0)   │
        └────────┬────────┘
                 │
        ┌────────┼────────┐
        ▼        ▼        ▼
      "76"     "7663"  "76638463"
       │         │         │
       ▼         ▼         ▼
     ["so"]   ["some",   ["sometime"]
              "rome"]      │
       │         │         │
       ▼         ▼         ▼
    backtrack(2) backtrack(4) backtrack(8)
       │         │         │
       ▼         ▼         ▼
      "63"     "8463"     (end)
       │         │
       ▼         ▼
     ["me"]   ["time"]
       │         │
       ▼         ▼
    backtrack(4) backtrack(8)
       │         │
       ▼         ▼
      "8463"   (end)
       │
       ▼
     ["time"]
       │
       ▼
    backtrack(8)
       │
       ▼
     (end) → FOUND!
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| getWordCombinations | O(n * 2^n) worst | O(n * 2^n) |

Where n = length of digit sequence. Exponential due to all possible partitions.

### Key Points
- HashMap provides O(1) lookup for valid word prefixes
- Backtracking explores all valid segmentations
- Early pruning: only continue if prefix exists in map
- Words can be reused in different combinations

---

## File Reference
- **Source**: `src/main/java/org/example/T9WordCombinations.java`
- **Package**: `org.example`
- **Test Case**: `getWordCombinations([7,6,6,3,8,4,6,3], ["some","time","rome","sometime","so","me"])` → 4 combinations