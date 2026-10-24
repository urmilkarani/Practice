package org.example.neetcode;

import java.util.Arrays;

public class ProductOfArrayExceptSelf {
    public int[] productExceptSelf(int[] numbers) {
        int[] result = new int[numbers.length];
        int leftProduct = 1;
        for (int index = 0; index < numbers.length; index++) {
            result[index] = leftProduct;
            leftProduct *= numbers[index];
        }
        int rightProduct = 1;
        for (int index = numbers.length - 1; index >= 0; index--) {
            result[index] *= rightProduct;
            rightProduct *= numbers[index];
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new ProductOfArrayExceptSelf().productExceptSelf(new int[]{1, 2, 4, 6})));
    }
}
