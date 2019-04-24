/**
 * Given an array of integers, return a new array such that each element at index i of the new array
 * is the product of all the numbers in the original array except the one at i.
 *
 * For example, if our input was [1, 2, 3, 4, 5], the expected output would be [120, 60, 40, 30, 24].
 * If our input was [3, 2, 1], the expected output would be [2, 3, 6].
 *
 * Follow-up: what if you can't use division?
 */
package dailycodingproblems;

import java.util.Arrays;

public class DailyCodingProblem2 {
    public static void main (String[] args) {
        int[] arr = {1, 2, 3, 4, 5};
        double[] result = productOfArrayElements(arr);
        System.out.println(Arrays.toString(result));
    }

    private static double[] productOfArrayElements(int[] arr) {
        double[] result = new double[arr.length];
        int i = 0;
        while (i <= arr.length - 1) {
            result[i] = productOf(arr, 0, i - 1) * productOf(arr, i + 1, arr.length - 1);
            i++;
        }
        return result;
    }

    private static double productOf(int[] arr, int start, int end) {
        if(end < 0 || start > arr.length-1) {
            return 1;
        } else if (start == end) {
            return arr[start];
        } else {
            return arr[start]*productOf(arr,start+1,end);
        }
    }
}
