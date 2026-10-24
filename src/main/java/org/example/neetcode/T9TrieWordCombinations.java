package org.example.neetcode;

import java.util.*;

public class T9TrieWordCombinations {

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

    // Trie Node definition
    static class TrieNode {
        TrieNode[] children = new TrieNode[10]; // Index 0-9 for digits '0'-'9'
        List<String> words = new ArrayList<>();  // Words ending at this digit sequence
    }

    private final TrieNode root = new TrieNode();

    // Insert a word into the Digit Trie
    private void insert(String word) {
        TrieNode curr = root;
        for (char c : word.toLowerCase().toCharArray()) {
            if (c >= 'a' && c <= 'z') {
                int digit = T9_MAP[c - 'a'] - '0';
                if (curr.children[digit] == null) {
                    curr.children[digit] = new TrieNode();
                }
                curr = curr.children[digit];
            }
        }
        curr.words.add(word); // Store the word at the terminal digit node
    }

    public String[][] getWordCombinations(int[] digits, String[] validWords) {
        // 1. Build the Trie from valid words
        for (String word : validWords) {
            insert(word);
        }

        // 2. Perform backtracking using the Trie
        List<List<String>> combinations = new ArrayList<>();
        backtrack(digits, 0, new ArrayList<>(), combinations);

        // 3. Convert result to String[][]
        String[][] result = new String[combinations.size()][];
        for (int i = 0; i < combinations.size(); i++) {
            result[i] = combinations.get(i).toArray(new String[0]);
        }

        return result;
    }

    private void backtrack(int[] digits, int start, List<String> currentPath, List<List<String>> result) {
        // Base Case: All digits consumed
        if (start == digits.length) {
            result.add(new ArrayList<>(currentPath));
            return;
        }

        TrieNode curr = root;

        // Traverse down the Trie starting from index `start`
        for (int i = start; i < digits.length; i++) {
            int digit = digits[i];

            // Early Pruning: No words exist with this digit prefix
            if (curr.children[digit] == null) {
                break;
            }

            curr = curr.children[digit];

            // If words match this prefix sequence, try them
            if (!curr.words.isEmpty()) {
                for (String word : curr.words) {
                    currentPath.add(word);
                    backtrack(digits, i + 1, currentPath, result);
                    currentPath.remove(currentPath.size() - 1); // Backtrack
                }
            }
        }
    }

    public static void main(String[] args) {
        T9TrieWordCombinations solver = new T9TrieWordCombinations();

        int[] digits = {7, 6, 6, 3, 8, 4, 6, 3};
        String[] validWords = {"some", "time", "rome", "sometime", "so", "me"};

        String[][] combinations = solver.getWordCombinations(digits, validWords);

        // Print output
        System.out.println(Arrays.deepToString(combinations));
    }
}
