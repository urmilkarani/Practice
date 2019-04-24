package leetcode;

import java.util.HashSet;
import java.util.Set;

public class IntegerPalindrome {
    public static void main(String args[]) {
        int input = 121;
        boolean result = isPalindrome(input);
        System.out.println(result);
    }

    private static boolean isPalindrome(int x) {
        boolean isPalindrome = false;
        int temp2 = 0;
        int reverse = 0;
        int sign = 0;
        if (x < 0) {
            return isPalindrome;
        }
        int temp = x;
        while (temp != 0) {
            int tail = temp%10;
            temp2 = temp2*10;
            temp2 = temp2 + tail;
            if((temp2 - tail)/10 != reverse) {
                reverse = 0;
            }
            reverse = temp2;
            temp = temp/10;
        }
        return x == reverse;
    }


}
