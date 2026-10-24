package org.example.neetcode;

public class NeetCodeMinimumWindowSubstring {
    public String minWindow(String source, String target) {
        int[] required = new int[128];
        for (char character : target.toCharArray()) required[character]++;

        int left = 0, matched = 0, minimumLength = Integer.MAX_VALUE;
        String result = "";
        for (int right = 0; right < source.length(); right++) {
            if (--required[source.charAt(right)] >= 0) matched++;
            while (matched == target.length()) {
                if (right - left + 1 < minimumLength) {
                    minimumLength = right - left + 1;
                    result = source.substring(left, right + 1);
                }
                if (++required[source.charAt(left++)] > 0) matched--;
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(new NeetCodeMinimumWindowSubstring().minWindow("OUZODYXAZV", "XYZ"));
    }
}
