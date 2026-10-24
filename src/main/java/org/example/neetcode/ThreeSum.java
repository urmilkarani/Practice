package org.example.neetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ThreeSum {
    public List<List<Integer>> threeSum(int[] numbers) {
        Arrays.sort(numbers);
        List<List<Integer>> result = new ArrayList<>();
        for (int index = 0; index < numbers.length; index++) {
            if (numbers[index] > 0) break;
            if (index > 0 && numbers[index] == numbers[index - 1]) continue;
            int left = index + 1, right = numbers.length - 1;
            while (left < right) {
                int sum = numbers[index] + numbers[left] + numbers[right];
                if (sum > 0) right--;
                else if (sum < 0) left++;
                else {
                    result.add(Arrays.asList(numbers[index], numbers[left++], numbers[right--]));
                    while (left < right && numbers[left] == numbers[left - 1]) left++;
                }
            }
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(new ThreeSum().threeSum(new int[]{-1, 0, 1, 2, -1, -4}));
    }
}
