# ProductOfArrayExceptSelf - Visual Documentation

## Problem Statement
Given an integer array `numbers`, return an array `result` such that `result[i]` is equal to the product of all the elements of `numbers` except `numbers[i]`. Solve without division and in O(n) time.

## Algorithm: Prefix and Suffix Products

### Core Idea
Compute prefix products (left to right) and suffix products (right to left) in two passes. The result at each index is the product of prefix and suffix.

### Visualization

#### Example: numbers = [1, 2, 4, 6]

```
Pass 1: Left to Right (Prefix Products)
result[i] = product of all elements to the LEFT of i

index=0: leftProduct=1, result[0]=1, leftProduct=1*1=1
index=1: leftProduct=1, result[1]=1, leftProduct=1*2=2
index=2: leftProduct=2, result[2]=2, leftProduct=2*4=8
index=3: leftProduct=8, result[3]=8, leftProduct=8*6=48

After Pass 1: result = [1, 1, 2, 8]

Pass 2: Right to Left (Suffix Products)
result[i] *= product of all elements to the RIGHT of i

index=3: rightProduct=1, result[3]=8*1=8, rightProduct=1*6=6
index=2: rightProduct=6, result[2]=2*6=12, rightProduct=6*4=24
index=1: rightProduct=24, result[1]=1*24=24, rightProduct=24*2=48
index=0: rightProduct=48, result[0]=1*48=48, rightProduct=48*1=48

Final result: [48, 24, 12, 8]
```

### Visual Breakdown

```
numbers:    [1,  2,  4,  6]
             ↓  ↓  ↓  ↓
Prefix:     [1,  1,  2,  8]   (product of elements before)
Suffix:     [48, 24, 6,  1]   (product of elements after)
             ↓  ↓  ↓  ↓
Result:     [48, 24, 12, 8]   (prefix × suffix)

Verification:
result[0] = 2×4×6 = 48 ✓
result[1] = 1×4×6 = 24 ✓
result[2] = 1×2×6 = 12 ✓
result[3] = 1×2×4 = 8  ✓
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| productExceptSelf | O(n) | O(1) extra |

Note: Output array doesn't count as extra space.

### Key Points
- Two passes: left→right, then right→left
- No division used
- O(1) extra space (output array excluded)
- Each element gets product of all others

---

## File Reference
- **Source**: `src/main/java/org/example/ProductOfArrayExceptSelf.java`
- **Package**: `org.example`
- **Test Case**: `productExceptSelf([1,2,4,6])` → `[48, 24, 12, 8]`