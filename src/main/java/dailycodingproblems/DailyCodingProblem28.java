/**
 * Write an algorithm to justify text. Given a sequence of words and an integer line length k,
 * return a list of strings which represents each line, fully justified.
 *
 * More specifically, you should have as many words as possible in each line.
 * There should be at least one space between each word.
 * Pad extra spaces when necessary so that each line has exactly length k.
 * Spaces should be distributed as equally as possible, with the extra spaces, if any,
 * distributed starting from the left.
 *
 * If you can only fit one word on a line, then you should pad the right-hand side with spaces.
 *
 * Each word is guaranteed not to be longer than k.
 *
 * For example, given the list of words ["the", "quick", "brown", "fox", "jumps", "over", "the", "lazy", "dog"]
 * and k = 16, you should return the following:
 *
 * ["the  quick brown", # 1 extra space on the left
 * "fox  jumps  over", # 2 extra spaces distributed evenly
 * "the   lazy   dog"] # 4 extra spaces distributed evenly
 */
package dailycodingproblems;

import java.util.Arrays;
import java.util.List;

public class DailyCodingProblem28 {
    public static void main(String[] args) {
        String[] inputArray = {"tushar", "roy", "likes", "to", "code"};
        int stringLength = 10;
        String result = justifiedArray(inputArray,stringLength);
        System.out.println(result);
    }

    private static String justifiedArray(String[] inputArray, int stringLength) {
        int[][] table = new int[inputArray.length][inputArray.length];
        for(int i = 0 ; i < inputArray.length; i++) {
            table[i][i] = stringLength - inputArray[i].length();
            for ( int j = i+1; j < inputArray.length; j++) {
                table[i][j] = table[i][j-1] - inputArray[j].length() - 1;
            }
        }

        for(int i = 0 ; i < inputArray.length; i++) {
            for (int j = 0 ; j < inputArray.length; j++) {
                if(table[i][j] < 0) {
                    table[i][j] = Integer.MAX_VALUE;
                } else {
                    table[i][j] = (int) Math.pow(table[i][j],2);
                }
            }
            System.out.println("table ["+i+"]" + Arrays.toString(table[i]));
        }

        int minCost[] = new int[inputArray.length];
        int result[] = new int[inputArray.length];

        for(int i = inputArray.length-1; i >= 0; i--) {
            minCost[i] = table[i][inputArray.length-1];
            result[i] = inputArray.length;
            for(int j = inputArray.length - 1; j > i; j--){
                if(table[i][j-1] == Integer.MAX_VALUE) {
                    continue;
                }
                if(minCost[i] > minCost[j] + table[i][j-1]) {
                    minCost[i] = minCost[j] + table[i][j-1];
                    result[i] = j;
                }
            }
        }

        int i = 0;
        int j;
        System.out.println("Minimum Cost : "+ minCost[0]);
        StringBuilder builder = new StringBuilder();
        do{
            j = result[i];
            for(int k=i; k < j; k++){
                builder.append(inputArray[k] + " ");
            }
            builder.append("\n");
            i = j;
        }while(j < inputArray.length);

        return builder.toString();
    }
}
