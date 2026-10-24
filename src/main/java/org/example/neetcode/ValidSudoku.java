package org.example.neetcode;

import java.util.HashSet;
import java.util.Set;

public class ValidSudoku {
    public boolean isValidSudoku(char[][] board) {
        for (int index = 0; index < 9; index++) {
            if (!hasNoDuplicates(board[index])) return false;
            char[] column = new char[9];
            for (int row = 0; row < 9; row++) column[row] = board[row][index];
            if (!hasNoDuplicates(column)) return false;
        }
        for (int block = 0; block < 9; block++) {
            char[] square = new char[9];
            for (int offset = 0; offset < 9; offset++) {
                square[offset] = board[(block / 3) * 3 + offset / 3][(block % 3) * 3 + offset % 3];
            }
            if (!hasNoDuplicates(square)) return false;
        }
        return true;
    }

    private boolean hasNoDuplicates(char[] values) {
        Set<Character> seen = new HashSet<>();
        for (char value : values) if (value != '.' && !seen.add(value)) return false;
        return true;
    }

    public static void main(String[] args) {
        char[][] board = {
                {'1', '2', '.', '.', '3', '.', '.', '.', '.'},
                {'4', '.', '.', '5', '.', '.', '.', '.', '.'},
                {'.', '9', '8', '.', '.', '.', '.', '.', '3'},
                {'5', '.', '.', '.', '6', '.', '.', '.', '4'},
                {'.', '.', '.', '8', '.', '3', '.', '.', '5'},
                {'7', '.', '.', '.', '2', '.', '.', '.', '6'},
                {'.', '.', '.', '.', '.', '.', '2', '.', '.'},
                {'.', '.', '.', '4', '1', '9', '.', '.', '8'},
                {'.', '.', '.', '.', '8', '.', '.', '7', '9'}
        };
        System.out.println(new ValidSudoku().isValidSudoku(board));
    }
}
