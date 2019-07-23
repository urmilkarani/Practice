/**
 * You have an N by N board. Write a function that, given N, returns the number of possible arrangements of the
 * board where N queens can be placed on the board without threatening each other, i.e.
 * no two queens share the same row, column, or diagonal.
 */
package dailycodingproblems;

import java.util.Arrays;

public class DailyCodingProblem38 {
    public static int count = 0;
    public static void main(String[] args) {
        int n = 6;
        nQueens(n);
        System.out.println(count);
    }

    private static void nQueens(int n) {
        int[][] board = new int[n][n];
        for(int i = 0; i < n; i++) {
            for(int j = 0; j < n; j++) {
                board[i][j] = 0;
            }
        }
        nQueensUtil(board, 0);
    }

    private static boolean isSafe(int board[][], int row, int col)
    {
        int i, j;

        /* Check this row on left side */
        for (i = 0; i < col; i++)
            if (board[row][i] == 1)
                return false;

        /* Check upper diagonal on left side */
        for (i = row, j = col; i >= 0 && j >= 0; i--, j--)
            if (board[i][j] == 1)
                return false;

        /* Check lower diagonal on left side */
        for (i = row, j = col; j >= 0 && i < board.length; i++, j--)
            if (board[i][j] == 1)
                return false;

        return true;
    }

    private static void nQueensUtil(int board[][], int column) {
        int columnLength = board[0].length;
        if(column == columnLength) {
            printBoard(board);
            count++;
        }
        for(int i = 0; i < columnLength; i++) {
            if (isSafe(board, i, column)) {
                board[i][column] = 1;
                nQueensUtil(board, column + 1);
                board[i][column] = 0;
            }
        }
    }

    private static void printBoard(int[][] board) {
        System.out.println("Board Status :");
        for(int i = 0; i < board.length; i++) {
            System.out.println(Arrays.toString(board[i]));
        }
        System.out.println();
    }

    private static void resetBoard(int[][] board) {
        for(int i = 0 ; i < board.length; i++) {
            for(int j = 0; j < board[0].length; j++) {
                board[i][j] = 0;
            }
        }
    }
}