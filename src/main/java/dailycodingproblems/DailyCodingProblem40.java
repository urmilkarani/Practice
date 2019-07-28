/**
 * Given an array of integers where every integer occurs three times except for one integer,
 * which only occurs once, find and return the non-duplicated integer.
 *
 * For example, given [6, 1, 3, 3, 3, 6, 6], return 1. Given [13, 19, 13, 13], return 19.
 *
 * Do this in O(N) time and O(1) space.
 */
package dailycodingproblems;


public class DailyCodingProblem40 {
    private static final int INT_RANGE = 32;
    public static void main(String[] args) {
        int[] input = {6, 1, 3, 3, 3, 1, 1, 2, 6, 6};
        int result = returnInteger(input);
        System.out.println("Element occurring only once : "+result);
    }

    private static int returnInteger(int[] input) {
        int length = input.length;
        int ones = 0;
        int twos = 0;
        int moreThanThrees = 0;
        for(int i = 0; i < length; i++) {
            twos |= ones & input[i];
            ones ^= input[i];
            moreThanThrees = ~(ones & twos);
            ones &= moreThanThrees;
            twos &= moreThanThrees;
        }
        return ones;
    }
}
