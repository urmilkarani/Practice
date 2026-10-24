package org.example.neetcode;

public class ValidPalindrome {
    public boolean isPalindrome(String value) {
        int left = 0, right = value.length() - 1;
        while (left < right) {
            while (left < right && !Character.isLetterOrDigit(value.charAt(left))) left++;
            while (left < right && !Character.isLetterOrDigit(value.charAt(right))) right--;
            if (Character.toLowerCase(value.charAt(left)) != Character.toLowerCase(value.charAt(right))) return false;
            left++;
            right--;
        }
        return true;
    }

    public static void main(String[] args) {
        System.out.println(new ValidPalindrome().isPalindrome("Was it a car or a cat I saw?"));
    }
}
