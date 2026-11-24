# ValidSudoku - Visual Documentation

## Problem Statement
Determine if a 9x9 Sudoku board is valid. Only the filled cells need to be validated according to:
1. Each row must contain digits 1-9 without repetition
2. Each column must contain digits 1-9 without repetition
3. Each of the nine 3x3 sub-boxes must contain digits 1-9 without repetition

## Algorithm: HashSet Validation for Rows, Columns, and Boxes

### Core Idea
Check each row, column, and 3x3 box for duplicates using a HashSet. Empty cells ('.') are ignored.

### Visualization

#### Board Structure
```
Row indices: 0-8
Column indices: 0-8
Box indices: 0-8 (3x3 grid of boxes)

Box index calculation:
boxRow = (block / 3) * 3 + offset / 3
boxCol = (block % 3) * 3 + offset % 3
```

#### Box Layout
```
┌─────┬─────┬─────┐
│  0  │  1  │  2  │  ← block 0,1,2
├─────┼─────┼─────┤
│  3  │  4  │  5  │  ← block 3,4,5
├─────┼─────┼─────┤
│  6  │  7  │  8  │  ← block 6,7,8
└─────┴─────┴─────┘
```

#### Validation Process

```
For each index 0-8:
  1. Check row[index] for duplicates
  2. Check column[index] for duplicates
  3. Check box[block] for duplicates (block 0-8)

hasNoDuplicates(values):
  seen = HashSet()
  for value in values:
    if value != '.' and !seen.add(value):
      return false  // duplicate found
  return true
```

#### Example Board Check
```
Row 0: ['1','2','.','.','3','.','.','.','.'] → seen={1,2,3} ✓
Col 0: ['1','4','.','5','.','7','.','.','.'] → seen={1,4,5,7} ✓
Box 0: ['1','2','.','4','.','.','.','9','8'] → seen={1,2,4,9,8} ✓
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| isValidSudoku | O(1) | O(1) |

Note: Fixed 9x9 board, so technically O(1) but conceptually O(n²) for n×n board.

### Key Points
- Three separate validation passes (rows, columns, boxes)
- HashSet for O(1) duplicate detection
- Box index formula maps 1D block index to 2D coordinates
- Early exit on first duplicate found

---

## File Reference
- **Source**: `src/main/java/org/example/ValidSudoku.java`
- **Package**: `org.example`
- **Test Case**: 9x9 board with mixed digits and '.' → validates correctly