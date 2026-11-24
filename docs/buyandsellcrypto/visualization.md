# BuyAndSellCrypto - Visual Documentation

## Problem Statement
Given an array `prices` where `prices[i]` is the price of a cryptocurrency on day `i`, find the maximum profit from a single buy-sell transaction. You must buy before you sell.

## Algorithm: Two Pointers (Sliding Window)

### Core Idea
Maintain a `left` pointer at the minimum price seen so far (buy day). For each `right` pointer (sell day), calculate profit and update maximum. If a lower price is found, move `left` to `right`.

### Visualization

#### Example: prices = [10, 1, 5, 6, 7, 1]

```
Initial: left=0 (price=10), result=0

right=1, price=1:
  prices[left]=10 > prices[right]=1 → left=right=1
  result = max(0, 1-1) = 0

right=2, price=5:
  prices[left]=1 < prices[right]=5
  result = max(0, 5-1) = 4

right=3, price=6:
  prices[left]=1 < prices[right]=6
  result = max(4, 6-1) = 5

right=4, price=7:
  prices[left]=1 < prices[right]=7
  result = max(5, 7-1) = 6

right=5, price=1:
  prices[left]=1 >= prices[right]=1 → left=right=5
  result = max(6, 1-1) = 6

Final result = 6 (buy at 1, sell at 7)
```

### Visual Representation

```
Prices:    [10, 1, 5, 6, 7, 1]
Index:      0  1  2  3  4  5
            ↑     ↑
          left  right (best: buy at 1, sell at 7)

left pointer tracks minimum price so far
right pointer scans forward for sell opportunities
```

### Step-by-Step Table

| right | price | left | prices[left] | profit | result | action |
|-------|-------|------|--------------|--------|--------|--------|
| 0 | 10 | 0 | 10 | - | 0 | init |
| 1 | 1 | 1 | 1 | 0 | 0 | new min, move left |
| 2 | 5 | 1 | 1 | 4 | 4 | update max |
| 3 | 6 | 1 | 1 | 5 | 5 | update max |
| 4 | 7 | 1 | 1 | 6 | 6 | update max |
| 5 | 1 | 5 | 1 | 0 | 6 | new min, move left |

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| maxProfit | O(n) | O(1) |

### Key Points
- Single pass through prices
- `left` always points to minimum price seen so far
- Calculate profit at each step
- Update `left` when new minimum found
- Equivalent to Kadane's algorithm on price differences

---

## File Reference
- **Source**: `src/main/java/org/example/BuyAndSellCrypto.java`
- **Package**: `org.example`
- **Test Case**: `maxProfit([10,1,5,6,7,1])` → `6`