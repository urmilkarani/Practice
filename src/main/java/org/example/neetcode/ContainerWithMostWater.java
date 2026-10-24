package org.example.neetcode;

public class ContainerWithMostWater {
    public int maxArea(int[] heights) {
        int result = 0, left = 0, right = heights.length - 1;
        while (left < right) {
            result = Math.max(result, (right - left) * Math.min(heights[left], heights[right]));
            if (heights[left] >= heights[right]) right--;
            else left++;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(new ContainerWithMostWater().maxArea(new int[]{1, 7, 2, 5, 4, 7, 3, 6}));
    }
}
