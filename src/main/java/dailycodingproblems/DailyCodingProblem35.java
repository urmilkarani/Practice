/**
 * Given an array of strictly the characters 'R', 'G', and 'B', segregate the values of the array so that all
 * the Rs come first, the Gs come second, and the Bs come last. You can only swap elements of the array.
 *
 * Do this in linear time and in-place.
 *
 * For example, given the array ['G', 'B', 'R', 'R', 'B', 'R', 'G'], it should become
 * ['R', 'R', 'R', 'G', 'G', 'B', 'B'].
 */
package dailycodingproblems;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class DailyCodingProblem35 {
    public static void main(String[] args) {
        String[] input = {"G", "B", "R", "R", "B", "R", "G"};
        String[] result = sortedArray(input);
        System.out.println(Arrays.toString(result));
    }

    private static String[] sortedArray(String[] input) {
        int length = input.length;
        HashMap<String, List<Integer>> indexMap = new HashMap<>();
        for(int i = 0; i < length; i++) {
            ArrayList<Integer> indexList;
            if(indexMap.containsKey(input[i])) {
                indexList = (ArrayList<Integer>)indexMap.get(input[i]);
                indexList.add(i);
            } else {
                indexList = new ArrayList<>();
                indexList.add(i);
            }
            indexMap.put(input[i],indexList);
        }
        int lastIndexOfR = indexMap.get("R").size()-1;
        int lastIndexOfG = lastIndexOfR + indexMap.get("G").size();
        int lastIndexOfB = lastIndexOfG + indexMap.get("B").size();
        
        return null;
    }

    private void swap(String stringOne, String stringTwo) {
        String temp;
        temp = stringOne;
        stringOne = stringTwo;
        stringTwo = temp;
    }
}
