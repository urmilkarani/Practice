# TrappingRainWater - Visual Documentation

## Problem Statement
Given `n` non-negative integers representing an elevation map where the width of each bar is 1, compute how much water it can trap after raining.

## Algorithm: Two Pointers with Max Tracking

### Core Idea
Use two pointers at both ends, tracking the maximum height seen from left and right. Water trapped at each position depends on the minimum of leftMax and rightMax.

### Visualization

#### Example: heights = [0, 2, 0, 3, 1, 0, 1, 3, 2, 1]

```
Initial: left=0, right=9, leftMax=0, rightMax=1, result=0

Step 1: leftMax(0) < rightMax(1)
  left++ → left=1, height=2
  leftMax = max(0,2) = 2
  result += 2 - 2 = 0

Step 2: leftMax(2) >= rightMax(1)
  right-- → right=8, height=2
  rightMax = max(1,2) = 2
  result += 2 - 2 = 0

Step 3: leftMax(2) >= rightMax(2)
  right-- → right=7, height=3
  rightMax = max(2,3) = 3
  result += 3 - 3 = 0

Step 4: leftMax(2) < rightMax(3)
  left++ → left=2, height=0
  leftMax = max(2,0) = 2
  result += 2 - 0 = 2  ← Water trapped!

Step 5: leftMax(2) < rightMax(3)
  left++ → left=3, height=3
  leftMax = max(2,3) = 3
  result += 3 - 3 = 0

Step 6: leftMax(3) >= rightMax(3)
  right-- → right=6, height=1
  rightMax = max(3,1) = 3
  result += 3 - 1 = 2  ← Water trapped!

Step 7: leftMax(3) >= rightMax(3)
  right-- → right=5, height=0
  rightMax = max(3,0) = 3
  result += 3 - 0 = 3  ← Water trapped!

Step 8: leftMax(3) >= rightMax(3)
  right-- → right=4, height=1
  rightMax = max(3,1) = 3
  result += 3 - 1 = 2  ← Water trapped!

Step 9: left=3, right=4, leftMax=3, rightMax=3
  leftMax >= rightMax
  right-- → right=3, stop (left >= right)

Total result = 2 + 2 + 3 + 2 = 9
```

### Visual Representation

```
Heights:     [0, 2, 0, 3, 1, 0, 1, 3, 2, 1]
Index:        0  1  2  3  4  5  6  7  8  9

Water level at each position (min of leftMax, rightMax):
leftMax:  [0, 2, 2, 3, 3, 3, 3, 3, 3, 3]
rightMax: [3, 3, 3, 3, 3, 3, 3, 3, 2, 1]
min:      [0, 2, 2, 3, 3, 3, 3, 3, 2, 1]
height:   [0, 2, 0, 3, 1, 0, 1, 3, 2, 1]
water:    [0, 0, 2, 0, 2, 3, 2, 0, 0, 0]  ← min - height
Total: 9
```

### Why Two Pointers Works
```
At any point, we know:
- leftMax = max height from left to current left
- rightMax = max height from right to current right

If leftMax < rightMax:
  Water at left is bounded by leftMax (right side has higher wall)
  We can safely calculate water at left and move left++

If leftMax >= rightMax:
  Water at right is bounded by rightMax
  We can safely calculate water at right and move right--
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| trap | O(n) | O(1) |

### Key Points
- Two pointers converge from both ends
- Track max heights from both sides
- Water trapped = min(leftMax, rightMax) - height[i]
- Single pass, constant space

---

## File Reference
- **Source**: `src/main/java/org/example/TrappingRainWater.java`
- **Package**: `org.example`
- **Test Case**: `trap([0,2,0,3,1,0,1,3,2,1])` → `9`