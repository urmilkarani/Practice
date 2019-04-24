package leetcode;

import java.io.*;
import java.util.*;
import java.text.*;
import java.math.*;
import java.util.regex.*;

// runlength encoding
// 
// Input: AAAAAAABCFFFFFAA
// Output: A7B1C1F5A2

public class Solution {
    public static void main(String args[] ) throws Exception {
        /* Enter your code here. Read input from STDIN. Print output to STDOUT */
        BufferedReader bufferReader = new BufferedReader(new InputStreamReader(System.in));
        String input = bufferReader.readLine();
        String result = runlengthEncoding(input);
        System.out.println("Encoded String: " + result);
    }

    private static String runlengthEncoding(String input) {
        if(input.length() == 0) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        int length = input.length();
        char[] charArray = input.toCharArray();
        int count = 0;
        for(int i = 0; i < length; i++) {
            count++;
            if((i+1) >= length || charArray[i] != charArray[i+1]) {
                str.append(charArray[i]);
                str.append(count);
                count = 0;
            }
        }
        return str.toString();
    }
}