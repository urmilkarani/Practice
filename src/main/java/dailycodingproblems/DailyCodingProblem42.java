/**
 * Given a list of integers S and a target number k, write a function that returns a subset of S that adds up to k. 
 * If such a subset cannot be made, then return null.
 *
 * Integers can appear more than once in the list. You may assume all numbers in the list are positive.
 *
 * For example, given S = [12, 1, 61, 5, 9, 2] and k = 24, return [12, 9, 2, 1] since it sums up to 24
 */
package dailycodingproblems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DailyCodingProblem42 {
    public static void main(String[] args) {
        int k = 24;
        int[] S = {12,1,61,5,9,2};
        List<Integer> result = findSubsetSum(S,k);
        if(!result.isEmpty()) {
            System.out.println("Result: " + result);
        } else {
            System.out.println("No Such Subset Found");
        }
    }

    private static List<Integer> findSubsetSum(int[] s, int k) {
        List<Integer> resultList = new ArrayList<>();
        boolean[][] table = new boolean[s.length+1][k+1];
        if (isSubsetPresent(table,s,k)) {
            resultList = populateResultList(table,s,k);
        }
        return resultList;
    }

    private static List<Integer> populateResultList(boolean[][] table, int[] s, int k) {
        List<Integer> resultList = new ArrayList<>();
        int i = s.length;
        int j = k;
        while(i >= 1 && j >=0) {
            if (table[i-1][j]) {
                i = i-1;
            } else {
                resultList.add(s[i-1]);
                j = j - s[i-1];
                i = i-1;
            }
        }
        return resultList;
    }

    private static boolean isSubsetPresent(boolean[][] table, int[] s, int k) {
        for(int i = 0; i <= s.length; i++) {
            table[i][0] = true;
        }
        //printBoard(table);
        for(int i = 1; i <= s.length ; i++) {
            for(int j = 1; j <= k; j++) {
                if(j < s[i-1]) {
                    table[i][j] = table[i-1][j];
                } else {
                    table[i][j] = table[i-1][j] || table[i-1][j-s[i-1]];
                }
            }
            //printBoard(table);
        }
        //printBoard(table);
        return table[s.length][k];
    }

    private static void printBoard(boolean[][] board) {
        System.out.println("Table Status:");
        for(int i = 0; i < board.length; i++) {
            System.out.println(Arrays.toString(board[i]));
        }
        System.out.println();
    }
}
