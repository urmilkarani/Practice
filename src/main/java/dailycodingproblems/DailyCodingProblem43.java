/**
 * Implement a stack that has the following methods:
 *      push(val), which pushes an element onto the stack
 *      pop(), which pops off and returns the topmost element of the stack. If there are no elements in the stack,
 *      then it should throw an error or return null.
 *      max(), which returns the maximum value in the stack currently. If there are no elements in the stack,
 *      then it should throw an error or return null.
 * Each method should run in constant time
 */
package dailycodingproblems;

import java.util.Stack;

class MaxStack extends Stack{
    Stack<Integer> maxStack;

    public MaxStack() {
        maxStack = new Stack<>();
    }

    @Override
    public Object pop() {
        Object popped = super.pop();
        if(!maxStack.isEmpty() && maxStack.peek() == popped) {
            maxStack.pop();
        }
        return popped;
    }

    @Override
    public Object push(Object value) {
        if(maxStack.isEmpty()) {
            maxStack.push((int) value);
        }
        if(!maxStack.isEmpty() && (int)value > maxStack.peek()) {
            maxStack.push((int) value);
        }
        super.push(value);
        return null;
    }

    public int getMax() {
         return !maxStack.isEmpty() ? maxStack.peek() : 0;
    }
}
public class DailyCodingProblem43 {
    public static void main(String[] args) {
        MaxStack maxStack = new MaxStack();
        maxStack.push(1);
        System.out.println("Max value: "+maxStack.getMax());
        maxStack.push(7);
        System.out.println("Max value: "+maxStack.getMax());
        maxStack.push(4);
        System.out.println("Max value: "+maxStack.getMax());
        maxStack.push(9);
        System.out.println("Max value: "+maxStack.getMax());
        int elementPopped = (int) maxStack.pop();
        System.out.println("Popped : " + elementPopped);
        System.out.println("Max value: "+maxStack.getMax());
        maxStack.push(23);
        System.out.println("Max value: "+maxStack.getMax());
        int elementPopped1 = (int) maxStack.pop();
        System.out.println("Popped : " + elementPopped1);
        System.out.println("Max value: "+maxStack.getMax());
        int elementPopped2 = (int) maxStack.pop();
        System.out.println("Popped : " + elementPopped2);
        System.out.println("Max value: "+maxStack.getMax());
    }
}
