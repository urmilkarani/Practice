package leetcode;

import java.util.*;

public class LetterCombination {

    public static void main(String[] args) {
        String str = "23";
        List<String> result = letterCombination(str);
        System.out.println(Arrays.toString(result.toArray()));
    }

    private static List<String> letterCombination(String digits) {
        List<String> ans = new LinkedList<String>();
        if (digits.isEmpty())
            return ans;
        String[] mapping = new String[]{"0", "1", "abc", "def", "ghi", "jkl", "mno", "pqrs", "tuv", "wxyz"};
        ans.add(0, "");
        for (int i = 0; i < digits.length(); i++) {
            int x = Character.getNumericValue(digits.charAt(i));
            int size = ans.size();     // number of nodes/strings already in the queue
            for (int k = 1; k <= size; k++) {
                String t = ans.remove(0);
                for (char s : mapping[x].toCharArray())
                    ans.add(t + s);
            }
        }
        return ans;
    }
}
