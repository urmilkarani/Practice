/**
 * Given a string, find the palindrome that can be made by inserting the fewest number of characters as possible
 * anywhere in the word. If there is more than one palindrome of minimum length that can be made,
 * return the lexicographically earliest one (the first one alphabetically).
 *
 * For example, given the string "race", you should return "ecarace", since we can add three letters to it
 * (which is the smallest amount to make a palindrome). There are seven other palindromes that can be made from
 * "race" by adding three letters, but "ecarace" comes first alphabetically.
 *
 * As another example, given the string "google", you should return "elgoogle".
 */
package dailycodingproblems;

import java.util.PriorityQueue;

public class DailyCodingProblem34 {
    public static void main(String[] args) {
        String input = "race";
        String palindrome = findPalindromeMinimumInserts(input);
        System.out.println("Palindrome: " + palindrome);
    }

    private static String findPalindromeMinimumInserts(String input) {
        String longestPalinSubString = longestPalindrome(input);
        int startIndex = input.indexOf(longestPalinSubString);
        int endIndex = startIndex+longestPalinSubString.length();
        String prefix = input.substring(0,startIndex);
        String suffix = input.substring(endIndex);
        StringBuilder appendStart = new StringBuilder(prefix);
        StringBuilder appendEnd = new StringBuilder(suffix);
        if(appendStart.toString().isEmpty()) {
            for(char c : appendEnd.toString().toCharArray()) {
                appendStart.insert(0,c);
            }
            return appendStart.append(input).toString();
        } else if (appendEnd.toString().isEmpty()) {
            for(char c : appendStart.toString().toCharArray()) {
                appendEnd.insert(0,c);
            }
            return input+appendEnd.toString();
        } else {
            String str = findLexicographicallySmallestString(appendStart.toString(), appendEnd.toString());
            StringBuffer reverseString = new StringBuffer(str).reverse();
            return str + longestPalinSubString + reverseString.toString();
        }
    }

    private static String findLexicographicallySmallestString(String appendStart, String appendEnd) {
        PriorityQueue<Character> stringPriorityQueue = new PriorityQueue<>();
        StringBuilder sb = new StringBuilder();
        for( char character: appendStart.toCharArray()) {
            stringPriorityQueue.add(character);
        }
        for(char character: appendEnd.toCharArray()) {
            stringPriorityQueue.add(character);
        }
        while(stringPriorityQueue.size() != 0) {
            sb.append(stringPriorityQueue.poll());
        }
        return sb.toString();
    }

    private static String longestPalindrome(String s) {
        if(s.length() == 0){
            return "";
        } else if(s.length() == 1) {
            return s;
        } else {
            int length = s.length();
            boolean [][] table = new boolean[length][length];
            int maxLength = 1;
            for (int i = 0; i < length; i++) {
                table[i][i] = true;
            }
            int start = 0;
            for (int i = 0; i < length-1; i++) {
                if (s.charAt(i) == s.charAt(i+1)) {
                    maxLength = 2;
                    start = i;
                    table[i][i+1] = true;
                }
            }
            for (int i = 3; i <= length; i++) {
                for (int j = 0; j < length - i + 1; j++)
                {
                    int k = j + i - 1;
                    if (table[j + 1][k - 1] && s.charAt(j) == s.charAt(k)) {
                        table[j][k] = true;

                        if (i > maxLength) {
                            start = j;
                            maxLength = i;
                        }
                    }
                }
            }
            if(maxLength == 1) {
                return s.substring(0,maxLength);
            }
            return s.substring(start, start + maxLength);
        }
    }
}
