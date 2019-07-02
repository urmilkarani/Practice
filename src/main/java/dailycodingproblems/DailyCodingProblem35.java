/**
 * Given an array of strictly the characters 'R', 'G', and 'B', segregate the values of the array so that all
 * the Rs come first, the Gs come second, and the Bs come last. You can only swap elements of the array.
 *
 * Do this in linear time and in-place.
 *
 * For example, given the array ['G', 'B', 'R', 'R', 'B', 'R', 'G'], it should become
 * ['R', 'R', 'R', 'G', 'G', 'B', 'B'].
 */
package dailycodingproblems;

import java.util.Arrays;

public class DailyCodingProblem35 {
    public static void main(String[] args) {
        String[] input = {"G", "B", "R", "R", "B", "R", "G", "G", "B", "R", "R", "B", "R", "G"};
        String[] result = sortedArray(input);
        System.out.println(Arrays.toString(result));
    }

    private static String[] sortedArray(String[] input) {
        int lo = 0;
        int hi = input.length-1;
        int mid = lo + (hi - lo)/2;
        while(mid <= hi) {
            if(input[mid].equals("R")) {
                swap(input, lo, mid);
                lo++;
            } else if(input[mid].equals("B")) {
                swap(input,mid,hi);
                hi--;
            } else {
                ++mid;
            }
        }

        return input;
    }

    private static void swap(String[] input, int lo, int mid) {
        String temp;
        temp = input[lo];
        input[lo] = input[mid];
        input[mid] = temp;
    }
}
