package leetcode;

public class CommonPrefix {

    public static void main(String args[]) {
        String[] strs = new String[]{"flower","flight","floor"};
        String result = findCommonPrefix(strs);
        System.out.println(result);
    }

    private static String findCommonPrefix(String[] strs) {
        if (strs == null || strs.length == 0)
            return "";
        if (strs.length == 1)
            return strs[0];

        int minLen = Integer.MAX_VALUE, minId = 0, n = strs.length;

        for (int i = 0; i < n; i++) {
            if (strs[i].length() < minLen) {
                minLen = strs[i].length();
                minId = i;
            }
        }

        String prefix = strs[minId];
        for (int i = 0; i < n; i++) {
            if (i != minId) {
                while (!strs[i].startsWith(prefix)) {
                    prefix = prefix.substring(0, prefix.length() - 1);
                }
            }
        }

        return prefix;
    }

}
