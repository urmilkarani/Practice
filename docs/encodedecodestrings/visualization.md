# EncodeDecodeStrings - Visual Documentation

## Problem Statement
Design an algorithm to encode a list of strings to a single string, and decode the single string back to the original list of strings. The encoded string may contain any characters including delimiters.

## Algorithm: Length-Prefix Encoding

### Core Idea
Prefix each string with its length followed by a delimiter (`#`). This allows unambiguous decoding since we know exactly how many characters to read for each string, regardless of content.

### Visualization

#### Example: values = ["neet", "code", "#"]

```
Encoding Process:
-----------------
Input: ["neet", "code", "#"]

Step 1: "neet" → length=4 → "4#neet"
Step 2: "code" → length=4 → "4#code"
Step 3: "#"    → length=1 → "1##"

Encoded: "4#neet4#code1##"
```

```
Decoding Process:
-----------------
Encoded: "4#neet4#code1##"

start=0: find '#' at index 1 → length=4 → read 4 chars "neet" → start=6
start=6: find '#' at index 7 → length=4 → read 4 chars "code" → start=12
start=12: find '#' at index 13 → length=1 → read 1 char "#" → start=15

Decoded: ["neet", "code", "#"]
```

### Step-by-Step Visual Breakdown

```
ENCODE:
-------
values:     ["neet", "code", "#"]
             ↓       ↓       ↓
length:      4       4       1
             ↓       ↓       ↓
encoded:  "4#neet" "4#code" "1##"
             ↓       ↓       ↓
Result:  "4#neet4#code1##"

DECODE:
-------
encoded:  "4#neet4#code1##"
           ↓
start=0:  find '#' at 1 → len=4 → "neet" → start=6
           ↓
start=6:  find '#' at 7 → len=4 → "code" → start=12
           ↓
start=12: find '#' at 13 → len=1 → "#" → start=15
           ↓
Result:  ["neet", "code", "#"]
```

### Edge Cases Handled

```
Empty string:     ""      → "0#"
String with #:    "a#b"   → "3#a#b"
String with digits: "123" → "3#123"
Multiple #:       "###"   → "3###"
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| encode    | O(n) | O(n)  |
| decode    | O(n) | O(n)  |

Where n = total length of all strings.

### Key Points
- Length prefix eliminates delimiter collision issues
- Works with any character including `#`, digits, empty strings
- Single pass for both encode and decode
- No escape sequences needed

---

## File Reference
- **Source**: `src/main/java/org/example/EncodeDecodeStrings.java`
- **Package**: `org.example`
- **Test Case**: `encode(["neet","code","#"])` → `"4#neet4#code1##"` → `decode` → `["neet","code","#"]`