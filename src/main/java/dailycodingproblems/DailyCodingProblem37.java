/**
 * The power set of a set is the set of all its subsets. Write a function that, given a set,
 * generates its power set.
 *
 * For example, given the set {1, 2, 3}, it should return {{}, {1}, {2}, {3}, {1, 2}, {1, 3}, {2, 3}, {1, 2, 3}}.
 *
 * You may also use a list or array to represent a set.
 */
package dailycodingproblems;

import java.util.ArrayList;
import java.util.List;

public class DailyCodingProblem37 {
    public static void main(String[] args) {
        List<Integer> arrayList = new ArrayList<Integer>(){{
            add(1);
            add(2);
            add(3);
        }};
        List<List<Integer>> resultList = powerSet(arrayList);
        resultList.stream().forEach(list -> System.out.println(list.toString()));
    }

    private static List<List<Integer>> powerSet(List<Integer> arrayList) {
        List<List<Integer>> resultList = new ArrayList<>();
        if(arrayList.isEmpty()) {
            resultList.add(new ArrayList<>());
            return resultList;
        }
        int length = arrayList.size();
        int powerSetSize =(int) Math.pow(2,length);
        for(int i = 0; i < powerSetSize; i++) {
            List<Integer> currentIntegerSet = new ArrayList<>();
            for(int j = 0; j < length; j++) {
                if((i & (1 << j)) != 0) {
                    currentIntegerSet.add(arrayList.get(j));
                }
            }
            resultList.add(currentIntegerSet);
        }
        return resultList;
    }
}
