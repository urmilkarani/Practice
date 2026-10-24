package org.example.neetcode;

import java.util.Arrays;
import java.util.PriorityQueue;

public class SlidingWindowMaximumPQ {

    void main() {
        int[] nums = slidingWindowMaximum(new int[]{1, 3, -1, -3, 5, 3, 6, 7}, 3);
        System.out.printf("Result: %s%n", Arrays.toString(nums));
    }

    private int[] slidingWindowMaximum(int[] nums, int k) {
        PriorityQueue<int[]> heap = new PriorityQueue<>((a, b) -> b[0] - a[0]);
        int[] output = new int[nums.length - k + 1];
        int idx = 0;
        for (int i = 0; i < nums.length; i++) {
            heap.offer(new int[]{nums[i], i});
            if (i >= k - 1) {
                while (heap.peek()[1] <= i - k) {
                    heap.poll();
                }
                output[idx++] = heap.peek()[0];
            }
        }
        return output;
    }
}
