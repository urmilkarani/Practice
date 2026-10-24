package org.example.neetcode;

public class PermutationInString {
    public boolean checkInclusion(String pattern, String value) {
        if (pattern.length() > value.length()) return false;
        int[] patternCounts = new int[26], windowCounts = new int[26];
        for (int index = 0; index < pattern.length(); index++) {
            patternCounts[pattern.charAt(index) - 'a']++;
            windowCounts[value.charAt(index) - 'a']++;
        }
        int matches = 0;
        for (int index = 0; index < 26; index++) if (patternCounts[index] == windowCounts[index]) matches++;

        for (int right = pattern.length(), left = 0; right < value.length(); right++, left++) {
            if (matches == 26) return true;
            int added = value.charAt(right) - 'a';
            windowCounts[added]++;
            if (windowCounts[added] == patternCounts[added]) matches++;
            else if (windowCounts[added] == patternCounts[added] + 1) matches--;

            int removed = value.charAt(left) - 'a';
            windowCounts[removed]--;
            if (windowCounts[removed] == patternCounts[removed]) matches++;
            else if (windowCounts[removed] == patternCounts[removed] - 1) matches--;
        }
        return matches == 26;
    }

    public static void main(String[] args) {
        System.out.println(new PermutationInString().checkInclusion("abc", "lecabee"));
    }
}
