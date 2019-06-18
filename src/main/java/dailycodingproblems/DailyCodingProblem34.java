/**
 * Given a string, find the palindrome that can be made by inserting the fewest number of characters as possible
 * anywhere in the word. If there is more than one palindrome of minimum length that can be made, return the
 * lexicographically earliest one (the first one alphabetically).
 *
 * For example, given the string "race", you should return "ecarace", since we can add three letters to it
 * (which is the smallest amount to make a palindrome). There are seven other palindromes that can be made from
 * "race" by adding three letters, but "ecarace" comes first alphabetically.
 *
 * As another example, given the string "google", you should return "elgoogle".
 */
package dailycodingproblems;

import java.util.HashMap;

public class DailyCodingProblem34 {
    public static void main(String[] args) {
        String input = "abca";
        String palindrome = findPalindromeMinimumInserts(input);
        System.out.println("Palindrome: " + palindrome);
    }

    private static String findPalindromeMinimumInserts(String input) {
        int length = input.length();
        int start = 0;
        int end = length-1;
        StringBuilder appendAtStart = new StringBuilder();
        StringBuilder appendAtEnd = new StringBuilder();
        while(start < end) {
            if(input.charAt(start) != input.charAt(end)) {
                if(input.charAt(start) - input.charAt(end) > 0) {
                    appendAtStart.append(input.charAt(end));
                    end--;
                } else {
                    appendAtEnd.append(input.charAt(start));
                    start++;
                }
            } else {
                start++;
                end--;
            }
        }
        return appendAtStart.toString()+input+appendAtEnd.toString();
    }


}
