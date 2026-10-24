package org.example.neetcode;

public class MinimumWindowSubstring {

    String minimunWindowSubstring(String s, String t) {
        int[] arr = new int[128];
        char[] sArr = s.toCharArray();
        char[] tArr = t.toCharArray();

        for(char curr: tArr) {
            arr[curr]++;
        }

        int l = 0, r = 0;
        int minLength = Integer.MAX_VALUE;
        String result = "";
        int counter = 0;

        while(r < sArr.length) {
            char current = sArr[r];
            if(--arr[current] >=0) {
                counter++;
            }
            while(counter == tArr.length) {
                int currLength = r - l + 1;
                if(currLength < minLength) {
                    minLength = currLength;
                    result = s.substring(l,r+1);
                }
                char leftChar = sArr[l];
                if(++arr[leftChar] > 0) {
                    counter--;
                }
                l++;
            }
            r++;
        }
        return result;
    }


    void main() {
        IO.println(String.format("Result: %s", minimunWindowSubstring("ADOBECODEBANC", "ABC")));
    }
}
