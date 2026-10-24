package org.example.neetcode;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class TwoSum {
    public int[] twoSum(int[] numbers, int target) {
        Map<Integer, Integer> indices = new HashMap<>();
        for (int index = 0; index < numbers.length; index++) {
            int complement = target - numbers[index];
            if (indices.containsKey(complement)) return new int[]{indices.get(complement), index};
            indices.put(numbers[index], index);
        }
        return new int[0];
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new TwoSum().twoSum(new int[]{3, 4, 5, 6}, 7)));
    }
}
