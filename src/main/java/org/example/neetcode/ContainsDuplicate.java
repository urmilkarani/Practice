package org.example.neetcode;

import java.util.HashSet;
import java.util.Set;

public class ContainsDuplicate {
    public boolean hasDuplicate(int[] numbers) {
        Set<Integer> seen = new HashSet<>();
        for (int number : numbers) {
            if (!seen.add(number)) return true;
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(new ContainsDuplicate().hasDuplicate(new int[]{1, 2, 3, 3}));
    }
}
