package dailycodingproblems;

import java.util.Arrays;

/**
 * Given a list of numbers and a number k, return whether any two numbers from the list add up to k.
 *
 * For example, given [10, 15, 3, 7] and k of 17, return true since 10 + 7 is 17.
 *
 * Bonus: Can you do this in one pass?
 */

public class DailyCodingProblem1 {

    public static void main(String[] args) {
        int[] arr = {10, 15, 3, 9, 1, 3, 4, 7};
        int k = 9;
        boolean result = findSum(arr,k);
        System.out.println(result);
    }

    private static boolean findSum(int[] arr, int k) {
        Arrays.sort(arr);
        if(arr[0] + arr[arr.length-1] < k) {
            return false;
        }
        for (int i = 0, j=arr.length-1 ; i < j;) {
            if (arr[i] + arr[j] < k) {
                i++;
            } else if (arr[i]+arr[j] > k) {
                j--;
            } else {
                return true;
            }
        }
        return false;
    }
}
