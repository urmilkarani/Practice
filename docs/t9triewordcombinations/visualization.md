# T9TrieWordCombinations - Visual Documentation

## Problem Statement
Given a sequence of T9 keypad digits and a dictionary of valid words, find all possible combinations of valid words that match the digit sequence exactly. Uses a Trie for efficient prefix matching.

## Algorithm: Digit Trie + Backtracking

### Core Idea
1. Build a Trie where each node represents a digit (0-9) and stores words ending at that digit sequence
2. Use backtracking with Trie traversal for efficient prefix matching and early pruning
3. Only traverse paths that exist in the Trie, eliminating invalid prefixes immediately

### T9 Keypad Mapping
```
2: abc    3: def    4: ghi    5: jkl    6: mno
7: pqrs   8: tuv    9: wxyz
```

### Trie Structure Visualization

#### Example: validWords = ["some", "time", "rome", "sometime", "so", "me"]

```
Trie Construction:
------------------
Insert "some" (7663):
root → 7 → 6 → 6 → 3 [words: "some"]

Insert "time" (8463):
root → 8 → 4 → 6 → 3 [words: "time"]

Insert "rome" (7663):
root → 7 → 6 → 6 → 3 [words: "some", "rome"]

Insert "sometime" (76638463):
root → 7 → 6 → 6 → 3 → 8 → 4 → 6 → 3 [words: "sometime"]

Insert "so" (76):
root → 7 → 6 [words: "so"]

Insert "me" (63):
root → 6 → 3 [words: "me"]
```

```
Final Trie Structure:
---------------------
root
├── 7
│   └── 6 [words: "so"]
│       └── 6
│           └── 3 [words: "some", "rome"]
│               └── 8
│                   └── 4
│                       └── 6
│                           └── 3 [words: "sometime"]
└── 8
    └── 4
        └── 6
            └── 3 [words: "time"]
└── 6
    └── 3 [words: "me"]
```

### Visualization

#### Example: digits = [7,6,6,3,8,4,6,3]

```
Step 1: Build Trie (as shown above)

Step 2: Backtracking with Trie Traversal
----------------------------------------
backtrack([7,6,6,3,8,4,6,3], 0, [], [])

start=0, curr=root:
  i=0: digit=7 → curr=root.children[7] ✓
    curr.words=[] → no match
  i=1: digit=6 → curr=curr.children[6] ✓
    curr.words=["so"] → MATCH!
    → path=["so"]
    → backtrack(digits, 2, ["so"], [])
    
    start=2, curr=root:
      i=2: digit=6 → curr=root.children[6] ✓
        curr.words=[] → no match
      i=3: digit=3 → curr=curr.children[3] ✓
        curr.words=["me"] → MATCH!
        → path=["so", "me"]
        → backtrack(digits, 4, ["so", "me"], [])
        
        start=4, curr=root:
          i=4: digit=8 → curr=root.children[8] ✓
            curr.words=[] → no match
          i=5: digit=4 → curr=curr.children[4] ✓
            curr.words=[] → no match
          i=6: digit=6 → curr=curr.children[6] ✓
            curr.words=[] → no match
          i=7: digit=3 → curr=curr.children[3] ✓
            curr.words=["time"] → MATCH!
            → path=["so", "me", "time"]
            → backtrack(digits, 8, ["so", "me", "time"], [])
            → start=8 == len → FOUND: ["so", "me", "time"]
          (backtrack)
        (backtrack)
      (backtrack: i=4 digit=8 → root.children[8] exists but no words at intermediate nodes)
    (backtrack)
  
  i=2: digit=6 → curr=curr.children[6] ✓
    curr.words=[] → no match
  i=3: digit=3 → curr=curr.children[3] ✓
    curr.words=["some", "rome"] → MATCH!
    → path=["some"]
    → backtrack(digits, 4, ["some"], [])
    ... (same as above) → FOUND: ["some", "time"]
    
    → path=["rome"]
    → backtrack(digits, 4, ["rome"], [])
    ... (same as above) → FOUND: ["rome", "time"]
    (backtrack)
  
  i=4: digit=8 → curr=curr.children[8] ✓
    curr.words=[] → no match
  i=5: digit=4 → curr=curr.children[4] ✓
    curr.words=[] → no match
  i=6: digit=6 → curr=curr.children[6] ✓
    curr.words=[] → no match
  i=7: digit=3 → curr=curr.children[3] ✓
    curr.words=["sometime"] → MATCH!
    → path=["sometime"]
    → backtrack(digits, 8, ["sometime"], [])
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

### Trie Traversal Visual Flow

```
Digits: 7 6 6 3 8 4 6 3
         │ │ │ │ │ │ │ │
         ▼ ▼ ▼ ▼ ▼ ▼ ▼ ▼
        ┌─────────────────────────────────────┐
        │ backtrack(start=0, curr=root)       │
        └──────────────┬──────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
     i=0:7         i=1:6           i=3:3
     curr=7        curr=7→6        curr=7→6→6→3
     words=[]      words=["so"]    words=["some","rome"]
        │              │                  │
        ▼              ▼                  ▼
     continue      MATCH!             MATCH!
        │              │                  │
        ▼              ▼                  ▼
     i=2:6         backtrack(2)       backtrack(4)
        │              │                  │
        ▼              ▼                  ▼
     curr=7→6→6    (finds "me"       (finds "time"
     words=[]       at 6→3)           at 8→4→6→3)
        │              │                  │
        ▼              ▼                  ▼
     i=3:3         FOUND:           FOUND:
     curr=7→6→6→3  ["so","me",      ["some","time"]
     words=["some",  "time"]         ["rome","time"]
     "rome"]           │                  │
        │              ▼                  ▼
        ▼         (continue          (continue
     MATCH!        from start=4)       from start=4)
        │
        ▼
     backtrack(4)
        │
        ▼
     (finds "time" at 8→4→6→3)
        │
        ▼
     FOUND: ["sometime"]
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| getWordCombinations | O(n * 2^n) worst | O(W * L + n * 2^n) |

Where:
- n = length of digit sequence
- W = number of valid words
- L = average word length
- Trie space: O(W * L) for storing all words

### Key Points
- **Early Pruning**: If `curr.children[digit] == null`, break immediately - no words share this prefix
- **Efficient Prefix Lookup**: Trie traversal is O(1) per digit vs HashMap substring creation
- **Memory Efficient**: Shared prefixes stored once (e.g., "some" and "sometime" share 7→6→6→3)
- **Same Results**: Produces identical combinations to HashMap approach but faster for large dictionaries

---

## File Reference
- **Source**: `src/main/java/org/example/T9TrieWordCombinations.java`
- **Package**: `org.example`
- **Test Case**: `getWordCombinations([7,6,6,3,8,4,6,3], ["some","time","rome","sometime","so","me"])` → 4 combinations