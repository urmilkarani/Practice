package leetcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ThreeSumDuplicated {
    public static void main(String args[]) {
        BufferedReader bufferedReader  = new BufferedReader(new InputStreamReader(System.in));
        try {
            int numberOfStudents = Integer.parseInt(bufferedReader.readLine());
            String[] inputArray = bufferedReader.readLine().split("\\s");
            int[] skillSetArray  = new int[numberOfStudents];
            for(int i = 0; i < numberOfStudents; i++) {
                skillSetArray[i] = Integer.parseInt(inputArray[i]);
            }
            int threshold = Integer.parseInt(bufferedReader.readLine());
            int result = findAllGroupsWithThreeSum(numberOfStudents, skillSetArray, threshold);
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static int findAllGroupsWithThreeSum(int numberOfStudents, int[] skillSetArray, int threshold) {
        int count = 0;
        HashSet<Integer> hashMap = new HashSet<>();
        for(int i = 0; i < numberOfStudents-1; i++) {
            for (int j = i+1; j < numberOfStudents; j++) {
                int remaining = threshold - (skillSetArray[i]+skillSetArray[j]);
                if(hashMap.contains(remaining)) {
                    count += 1;
                } else {
                    hashMap.add(skillSetArray[j]);
                }
            }
        }
        return count;
    }
}
