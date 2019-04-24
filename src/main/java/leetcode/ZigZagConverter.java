package leetcode;

import java.util.ArrayList;
import java.util.List;

public class ZigZagConverter {

    public static void main(String args[]) {
        String str = "PAYPALISHIRING";
        int numRows = 4;
        String result = convertZigZag(str, numRows);
        System.out.println(result);
    }

    private static String convertZigZag(String str, int numRows) {
        int forwardCounter = 0;
        int backwardCounter = numRows - 2;
        List<StringBuilder> zigZagList = new ArrayList<>();
        StringBuilder finalResult = new StringBuilder();
        if (numRows == 1) {
            return str;
        } else {
            for ( int i = 0; i < numRows; i++) {
                zigZagList.add(new StringBuilder());
            }
            for (char c : str.toCharArray()) {
                if (forwardCounter < numRows) {
                    backwardCounter = numRows - 2;
                    zigZagList.get(forwardCounter).append(c);
                    forwardCounter++;
                } else if (forwardCounter == numRows && backwardCounter > 0) {
                    zigZagList.get(backwardCounter).append(c);
                    backwardCounter --;
                } else if (backwardCounter == 0) {
                    forwardCounter = 0;
                    zigZagList.get(forwardCounter).append(c);
                    forwardCounter++;
                } else {
                    backwardCounter = numRows - 2;
                }

            }
        }

        for ( int i = 0; i < zigZagList.size(); i++) {
            String string = zigZagList.get(i).toString();
            finalResult.append(string);
        }
        return finalResult.toString();
    }
}
