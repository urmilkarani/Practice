package leetcode;

public class LongestPalindromicSubstring {

    public static void main (String args[]) {
        String question = "babad";
        String result = findLongestPalindromicSubstring(question);
        System.out.println(result);
    }

    private static String findLongestPalindromicSubstring(String question) {
        int length = question.length();
        if(length == 0) {
            return "";
        } else if (length == 1) {
            return question;
        }
        boolean [][] table = new boolean[length][length];
        int maxLength = 1;
        for (int i = 0; i < length; i++) {
            table[i][i] = true;
        }
        int start = 0;
        for (int i = 0; i < length-1; i++) {
            if (question.charAt(i) == question.charAt(i+1)) {
                maxLength = 2;
                start = i;
                table[i][i+1] = true;
            }
        }

        for(int i = 3; i <= length; i++) {
            for (int j = 0 ; j < length - i + 1; j++) {
                int k = i + j - 1;
                if ( table[j+1][k-1] && question.charAt(j) == question.charAt(k)) {
                    table[j][k] = true;
                    if(i > maxLength) {
                        maxLength = i;
                        start = j;
                    }
                }
            }
        }
        return question.substring(start, start+maxLength);
    }
}
