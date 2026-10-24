package org.example.neetcode;

public class TrappingRainWater {
    public int trap(int[] heights) {
        if (heights == null || heights.length == 0) return 0;
        int left = 0, right = heights.length - 1, leftMax = heights[left], rightMax = heights[right], result = 0;
        while (left < right) {
            if (leftMax < rightMax) {
                left++;
                leftMax = Math.max(leftMax, heights[left]);
                result += leftMax - heights[left];
            } else {
                right--;
                rightMax = Math.max(rightMax, heights[right]);
                result += rightMax - heights[right];
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(new TrappingRainWater().trap(new int[]{0, 2, 0, 3, 1, 0, 1, 3, 2, 1}));
    }
}
