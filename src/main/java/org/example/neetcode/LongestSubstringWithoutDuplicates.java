package org.example.neetcode;

import java.util.HashMap;
import java.util.Map;

public class LongestSubstringWithoutDuplicates {
    public int lengthOfLongestSubstring(String value) {
        Map<Character, Integer> indices = new HashMap<>();
        int left = 0, result = 0;
        for (int right = 0; right < value.length(); right++) {
            char character = value.charAt(right);
            if (indices.containsKey(character)) left = Math.max(indices.get(character) + 1, left);
            indices.put(character, right);
            result = Math.max(result, right - left + 1);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(new LongestSubstringWithoutDuplicates().lengthOfLongestSubstring("zxyzxyz"));
    }
}
