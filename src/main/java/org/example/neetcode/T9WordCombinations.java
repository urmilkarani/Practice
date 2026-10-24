package org.example.neetcode;

import java.util.*;

public class T9WordCombinations {

    // T9 keypad mapping: index 0 = 'a', 1 = 'b', ..., 25 = 'z'
    private static final char[] T9_MAP = {
            '2', '2', '2', // a, b, c
            '3', '3', '3', // d, e, f
            '4', '4', '4', // g, h, i
            '5', '5', '5', // j, k, l
            '6', '6', '6', // m, n, o
            '7', '7', '7', '7', // p, q, r, s
            '8', '8', '8', // t, u, v
            '9', '9', '9', '9'  // w, x, y, z
    };

    public static String[][] getWordCombinations(int[] digits, String[] validWords) {
        // 1. Convert integer array to a single digit string
        StringBuilder sb = new StringBuilder();
        for (int digit : digits) {
            sb.append(digit);
        }
        String targetDigits = sb.toString();

        // 2. Map T9 digit sequences to lists of valid words
        Map<String, List<String>> digitToWords = new HashMap<>();
        for (String word : validWords) {
            String digitSeq = wordToDigits(word);
            digitToWords.computeIfAbsent(digitSeq, k -> new ArrayList<>()).add(word);
        }

        // 3. Perform backtracking to find all valid combinations
        List<List<String>> combinations = new ArrayList<>();
        backtrack(targetDigits, 0, digitToWords, new ArrayList<>(), combinations);

        // 4. Convert result to String[][]
        String[][] result = new String[combinations.size()][];
        for (int i = 0; i < combinations.size(); i++) {
            result[i] = combinations.get(i).toArray(new String[0]);
        }

        return result;
    }

    private static void backtrack(String target, int start, Map<String, List<String>> digitToWords,
                                  List<String> currentPath, List<List<String>> result) {
        // Base Case: We consumed all digits, valid path found
        if (start == target.length()) {
            result.add(new ArrayList<>(currentPath));
            return;
        }

        // Try all valid prefixes starting at `start`
        for (int end = start + 1; end <= target.length(); end++) {
            String subDigit = target.substring(start, end);

            if (digitToWords.containsKey(subDigit)) {
                for (String word : digitToWords.get(subDigit)) {
                    currentPath.add(word);
                    backtrack(target, end, digitToWords, currentPath, result);
                    currentPath.remove(currentPath.size() - 1); // Backtrack
                }
            }
        }
    }

    // Converts a word to its T9 numeric string
    private static String wordToDigits(String word) {
        StringBuilder sb = new StringBuilder();
        for (char c : word.toLowerCase().toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                sb.append(T9_MAP[c - 'a']);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        int[] digits = {7, 6, 6, 3, 8, 4, 6, 3};
        String[] validWords = {"some", "time", "rome", "sometime", "so", "me"};

        String[][] combinations = getWordCombinations(digits, validWords);

        // Print output
        System.out.println(Arrays.deepToString(combinations));
    }
}