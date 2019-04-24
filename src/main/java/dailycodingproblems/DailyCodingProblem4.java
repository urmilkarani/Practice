/**
 * Given an array of integers, find the first missing positive integer in linear time and constant space.
 * In other words, find the lowest positive integer that does not exist in the array.
 * The array can contain duplicates and negative numbers as well.
 *
 * For example, the input [3, 4, -1, 1] should give 2. The input [1, 2, 0] should give 3.
 *
 * You can modify the input array in-place.
 */
package dailycodingproblems;

import java.util.Arrays;

public class DailyCodingProblem4 {
    public static void main(String[] args) {
        int[] arr = {0,0,0,-1,1,1,1,5,4,1,2};
        System.out.println(findMissingPositive(arr));
    }

    private static int findMissingPositive(int[] arr) {
        int missingElement = 0;
        Arrays.sort(arr);
        for(int i = 1, j = 0 ; i <= arr.length; i++) {
            if(arr[j] == arr[i-1] || arr[i-1] < 0) {
                continue;
            } else if (arr[i-1] == missingElement) {
                j = i-1;
                missingElement++;
            } else {
                return missingElement;
            }
        }
        return missingElement;
    }
}
