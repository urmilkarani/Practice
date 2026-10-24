package org.example.neetcode;

import java.util.HashMap;
import java.util.Map;

public class LongestRepeatingCharacterReplacement {
    public int characterReplacement(String value, int replacements) {
        Map<Character, Integer> counts = new HashMap<>();
        int result = 0, left = 0, maxFrequency = 0;
        for (int right = 0; right < value.length(); right++) {
            char character = value.charAt(right);
            counts.put(character, counts.getOrDefault(character, 0) + 1);
            maxFrequency = Math.max(maxFrequency, counts.get(character));
            while ((right - left + 1) - maxFrequency > replacements) {
                char leftCharacter = value.charAt(left++);
                counts.put(leftCharacter, counts.get(leftCharacter) - 1);
            }
            result = Math.max(result, right - left + 1);
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(new LongestRepeatingCharacterReplacement().characterReplacement("XYYX", 2));
    }
}
