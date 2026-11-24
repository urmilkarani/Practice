# ContainerWithMostWater - Visual Documentation

## Problem Statement
Given an integer array `heights` where each element represents a vertical line at position i with height `heights[i]`, find two lines that together with the x-axis form a container that holds the most water.

## Algorithm: Two Pointers (Greedy)

### Core Idea
Use two pointers at both ends. The area is limited by the shorter line. Move the pointer at the shorter line inward, as moving the taller line would only decrease width without increasing height.

### Visualization

#### Example: heights = [1, 7, 2, 5, 4, 7, 3, 6]

```
Initial: left=0 (height=1), right=7 (height=6)
Area = (7-0) * min(1,6) = 7 * 1 = 7
Since left < right, move left++

left=1 (height=7), right=7 (height=6)
Area = (7-1) * min(7,6) = 6 * 6 = 36 ← MAX so far
Since right < left, move right--

left=1 (height=7), right=6 (height=3)
Area = (6-1) * min(7,3) = 5 * 3 = 15
Since right < left, move right--

left=1 (height=7), right=5 (height=7)
Area = (5-1) * min(7,7) = 4 * 7 = 28
Equal heights, move either (right--)

left=1 (height=7), right=4 (height=4)
Area = (4-1) * min(7,4) = 3 * 4 = 12
Since right < left, move right--

left=1 (height=7), right=3 (height=5)
Area = (3-1) * min(7,5) = 2 * 5 = 10
Since right < left, move right--

left=1 (height=7), right=2 (height=2)
Area = (2-1) * min(7,2) = 1 * 2 = 2
Since right < left, move right--

left=1, right=1: stop
```

### Visual Representation

```
Heights: [1, 7, 2, 5, 4, 7, 3, 6]
Index:    0  1  2  3  4  5  6  7

Best container: indices 1 and 5 (both height 7)
Width = 4, Height = 7, Area = 28

Or indices 1 and 7 (heights 7 and 6)
Width = 6, Height = 6, Area = 36 ← MAXIMUM
```

### Why Greedy Works
```
Moving the taller line:
- Width decreases
- Height stays same or decreases (limited by shorter line)
- Area can only decrease or stay same

Moving the shorter line:
- Width decreases
- Height might increase (if we find a taller line)
- Area might increase
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| maxArea | O(n) | O(1) |

### Key Points
- Two pointers from both ends
- Always move the pointer at the shorter line
- Proof: moving taller line cannot increase area
- Single pass, constant space

---

## File Reference
- **Source**: `src/main/java/org/example/ContainerWithMostWater.java`
- **Package**: `org.example`
- **Test Case**: `maxArea([1,7,2,5,4,7,3,6])` → `36`