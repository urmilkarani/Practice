/**
 * Given an integer k and a string s, find the length of the longest substring that contains
 * at most k distinct characters.
 *
 * For example, given s = "abcba" and k = 2, the longest substring with k distinct characters is "bcb".
 */
package dailycodingproblems;

import java.util.HashMap;
import java.util.Map;

public class DailyCodingProblem13 {
    public static void main(String[] args) {
        String str = "abcba";
        int k = 3;
        int result = longestSubstringWithKDistinct(str,k);
        System.out.println(result);
    }

    private static int longestSubstringWithKDistinct(String str, int k) {
        char[] chars = str.toCharArray();
        int maxLength = 0;
        int start = 0;
        int end = 0;
        Map<Character,Integer> characterIntegerMap = new HashMap<>();

        for(int i =0; i <str.length(); i++) {
            if(characterIntegerMap.containsKey(chars[i])) {
                characterIntegerMap.put(chars[i],characterIntegerMap.get(chars[i])+1);
            } else {
                characterIntegerMap.put(chars[i],1);
            }
            while(characterIntegerMap.size() > k) {
                if (characterIntegerMap.get(chars[start]) == 1) {
                    characterIntegerMap.remove(chars[start]);
                    ++start;
                } else {
                    characterIntegerMap.put(chars[start],characterIntegerMap.get(chars[start]) - 1);
                }
                //++start;
            }
            maxLength = Math.max(0,end-start+1);
            ++end;
        }
        return maxLength;
    }
}
