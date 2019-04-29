/**
 * Given a dictionary of words and a string made up of those words (no spaces), return the original sentence in a list.
 * If there is more than one possible reconstruction, return any of them. If there is no possible reconstruction,
 * then return null.
 *
 * For example, given the set of words 'quick', 'brown', 'the', 'fox', and the string "thequickbrownfox",
 * you should return ['the', 'quick', 'brown', 'fox'].
 *
 * Given the set of words 'bed', 'bath', 'bedbath', 'and', 'beyond', and the string "bedbathandbeyond",
 * return either ['bed', 'bath', 'and', 'beyond] or ['bedbath', 'and', 'beyond'].
 */
package dailycodingproblems;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DailyCodingProblem22 {
    public static void main(String[] args) {
        Set<String> wordSet = new HashSet<String>(){{
            add("the");
            add("quick");
            add("brown");
            add("fox");
            add("beyond");
        }};
        String sentence = "beyondthewall";
        List<String> resultList = wordBreak(wordSet,sentence);
    }

    private static List<String> wordBreak(Set<String> wordSet, String sentence) {
        List<String> resultList = new ArrayList<>();
        int length = sentence.length();
        int i = 0, j = 1;
        StringBuilder sb = new StringBuilder();
        while(i < length-1 && j <= length) {
            if(!wordSet.contains(sentence.substring(i,j))) {
                j++;
            } else {
                String presentString = sentence.substring(i,j);
                resultList.add(presentString);
                i = j;
                j++;
            }
        }

        for(String str : resultList) {
            sb.append(str);
        }
        if(resultList.isEmpty() || !sentence.equalsIgnoreCase(sb.toString())) {
            return null;
        }
        return resultList;
    }
}
