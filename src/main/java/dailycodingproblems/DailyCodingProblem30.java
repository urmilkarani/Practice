/**
 * You are given an array of non-negative integers that represents a two-dimensional elevation map
 * where each element is unit-width wall and the integer is the height.
 * Suppose it will rain and all spots between two walls get filled up.
 *
 * Compute how many units of water remain trapped on the map in O(N) time and O(1) space.
 *
 * For example, given the input [2, 1, 2], we can hold 1 unit of water in the middle.
 *
 * Given the input [3, 0, 1, 3, 0, 5], we can hold 3 units in the first index,
 * 2 in the second, and 3 in the fourth index (we cannot hold 5 since it would run off to the left),
 * so we can trap 8 units of water.
 */
package dailycodingproblems;

public class DailyCodingProblem30 {
    public static void main(String[] args) {
        int[] input = {2, 0, 4, 1, 2, 5};
        int result = getWaterUnitsFilled(input);
        System.out.println("Water Units Filled : " + result);
    }

    private static int getWaterUnitsFilled(int[] input) {
        int result = 0;
        int leftMaximum = 0;
        int rightMaximum = 0;
        int low = 0;
        int high = input.length - 1;
        while(low <= high) {
            if(leftMaximum < rightMaximum) {
                if(input[low] > leftMaximum) {
                    leftMaximum = input[low];
                } else {
                    result = result + leftMaximum - input[low];
                    low++;
                }
            } else {
                if(input[high] > rightMaximum) {
                    rightMaximum = input[high];
                } else {
                    result = result + rightMaximum - input[high];
                    high --;
                }
            }
        }
        return result;
    }
}
