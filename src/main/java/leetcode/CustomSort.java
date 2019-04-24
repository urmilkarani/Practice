package leetcode;

import java.util.*;

public class CustomSort {
    public static void main (String args[]) {
        List<Integer> list = Arrays.asList(4,5,6,5,4,3);
        System.out.println(list.toString());
        customSort(list);
    }

    private static void customSort(List<Integer> arr) {
        int length = arr.size();
        Map<Integer,Integer> count = new HashMap<>();
        Map<Integer,Set<Integer>> hm = new TreeMap<>();
        List<Integer> inSet = new ArrayList<>();
        for (int i=0; i<length; i++) {
            int input = arr.get(i);
            if(inSet.contains(input)) {
                int prevCount = count.get(input);
                Set<Integer> updateList = hm.get(prevCount);
                updateList.remove((Integer) input);
                prevCount += 1;
                if(hm.containsKey(prevCount)) {
                    Set<Integer> updateList1 = hm.get(prevCount);
                    updateList1.add(input);
                } else {
                    Set<Integer> arrList = new HashSet<>();
                    arrList.add(input);
                    hm.put(prevCount,arrList);
                }
            } else {
                inSet.add(input);
                int currCount = 1;
                count.put(input,currCount);
                if(hm.containsKey(currCount)) {
                    Set<Integer> updateList1 = hm.get(currCount);
                    updateList1.add(input);
                } else {
                    Set<Integer> arrList = new HashSet<>();
                    arrList.add(input);
                    hm.put(currCount,arrList);
                }
            }
        }
        Iterator iterator = hm.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            int key = (int) entry.getKey();
            Set<Integer> value = (Set<Integer>) entry.getValue();
            for (int i : value) {
                for(int j = 0; j<key; j++) {
                    System.out.println(i);
                }
            }
        }
    }
}
