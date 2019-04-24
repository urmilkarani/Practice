package leetcode;

import java.util.Stack;

public class ParanthesesMatching {
    public static void main(String[] args) {
        String input = "([{}])";
        System.out.println(isValid(input));
    }

    private static boolean isValid(String s) {
        if (s.length() == 0) {
            return true;
        }

        if ((s.length() % 2) != 0 ||
                ')' == s.charAt(0) ||
                ']' == s.charAt(0) ||
                '}' == s.charAt(0)) {
            return false;
        }

        Stack<Character> charStack = new Stack<Character>();
        for (int i = 0; i < s.length(); i++) {
            char current = s.charAt(i);
            if (current == '(' || current == '[' || current == '{') {
                charStack.push(current);
            } else {
                switch(current){
                    case ']':
                        if(!charStack.isEmpty() && charStack.pop() != '[') {
                            return false;
                        }
                        break;
                    case '}':
                        if(!charStack.isEmpty() && charStack.pop() != '{') {
                            return false;
                        }
                        break;
                    case ')':
                        if(!charStack.isEmpty() && charStack.pop() != '(') {
                            return false;
                        }
                        break;
                }
            }

        }
        return !charStack.isEmpty() ? false : true;
    }
}
