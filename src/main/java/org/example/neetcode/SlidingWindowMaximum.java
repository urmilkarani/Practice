package org.example.neetcode;

import java.util.Arrays;

public class SlidingWindowMaximum {

    void main() {
        int[] nums = slidingWindowMaximum(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3);
        System.out.printf("Result: %s%n", Arrays.toString(nums));
    }

    private int[] slidingWindowMaximum(int[] nums, int k) {
        int n = nums.length;
        if (n * k == 0) return new int[0];
        if (k == 1) return nums;

        int[] left = new int[n];
        left[0] = nums[0];
        int[] right = new int[n];
        right[n - 1] = nums[n - 1];

        for (int i = 1; i < n; i++) {
            left[i] = (i % k == 0) ? nums[i] : Math.max(left[i - 1], nums[i]);
            int j = n - i - 1;
            right[j] = (j % k == 0) ? nums[j] : Math.max(right[j + 1], nums[j]);
        }

        int[] output = new int[n - k + 1];
        for (int i = 0; i < n - k + 1; i++) {
            output[i] = Math.max(left[i + k - 1], right[i]);
        }
        return output;
    }
}
