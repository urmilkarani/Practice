# ValidParenthesis - Visual Documentation

## Problem Statement
Given a string `s` containing only characters '(', ')', '{', '}', '[' and ']', determine if the input string is valid. An input string is valid if:
1. Open brackets must be closed by the same type of brackets
2. Open brackets must be closed in the correct order
3. Every close bracket has a corresponding open bracket of the same type

## Algorithm: Stack with HashMap

### Core Idea
Use a stack to track opening brackets. When encountering a closing bracket, check if it matches the top of the stack. Use a HashMap to map closing brackets to their corresponding opening brackets.

### Visualization

#### Data Structures
```
Stack (LIFO): stores opening brackets
HashMap: closeToOpen = {')'→'(', ']'→'[', '}'→'{'}
```

#### Example 1: "()[]{}"
```
char='(': not in closeToOpen → push '(' → stack: ['(']
char=')': in closeToOpen, stack.peek()='(' matches → pop → stack: []
char='[': not in closeToOpen → push '[' → stack: ['[']
char=']': in closeToOpen, stack.peek()='[' matches → pop → stack: []
char='{': not in closeToOpen → push '{' → stack: ['{']
char='}': in closeToOpen, stack.peek()='{' matches → pop → stack: []
End: stack.isEmpty() → true
```

#### Example 2: "([)]"
```
char='(': push → stack: ['(']
char='[': push → stack: ['(', '[']
char=')': in closeToOpen, stack.peek()='[' ≠ '(' → return false
```

### Stack Visualization

```
Valid: "()[]{}"
Step 1: push '('     Stack: [ '(' ]
Step 2: pop '('      Stack: [ ]
Step 3: push '['     Stack: [ '[' ]
Step 4: pop '['      Stack: [ ]
Step 5: push '{'     Stack: [ '{' ]
Step 6: pop '{'      Stack: [ ]
Result: Empty → VALID

Invalid: "([)]"
Step 1: push '('     Stack: [ '(' ]
Step 2: push '['     Stack: [ '(', '[' ]
Step 3: see ')', top is '[' ≠ '(' → INVALID
```

### Time & Space Complexity
| Operation | Time | Space |
|-----------|------|-------|
| isValid   | O(n) | O(n)  |

### Key Points
- Stack ensures correct nesting order
- HashMap provides O(1) bracket matching
- Early exit on mismatch
- Final check: stack must be empty

---

## File Reference
- **Source**: `src/main/java/org/example/ValidParenthesis.java`
- **Package**: `org.example`
- **Test Cases**: 
  - `isValid("()[]{}")` → `true`
  - `isValid("(]")` → `false`
  - `isValid("([)]")` → `false`
  - `isValid("{[]}")` → `true`