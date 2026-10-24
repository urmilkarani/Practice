package org.example.neetcode;

import java.util.Arrays;

public class TwoSumII {
    public int[] twoSum(int[] sortedNumbers, int target) {
        int left = 0, right = sortedNumbers.length - 1;
        while (left < right) {
            int sum = sortedNumbers[left] + sortedNumbers[right];
            if (sum > target) right--;
            else if (sum < target) left++;
            else return new int[]{left + 1, right + 1};
        }
        return new int[0];
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new TwoSumII().twoSum(new int[]{1, 2, 3, 4}, 3)));
    }
}
