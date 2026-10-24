package org.example.neetcode;

import java.util.HashSet;
import java.util.Set;

public class LongestConsecutiveSequence {
    public int longestConsecutive(int[] numbers) {
        Set<Integer> numberSet = new HashSet<>();
        for (int number : numbers) numberSet.add(number);
        int longest = 0;
        for (int number : numberSet) {
            if (!numberSet.contains(number - 1)) {
                int length = 1;
                while (numberSet.contains(number + length)) length++;
                longest = Math.max(longest, length);
            }
        }
        return longest;
    }

    public static void main(String[] args) {
        System.out.println(new LongestConsecutiveSequence().longestConsecutive(new int[]{2, 20, 4, 10, 3, 4, 5}));
    }
}
