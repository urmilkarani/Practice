package leetcode;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toMap;

public class MinimumNearestDeliveries {

    public static void main(String args[]) {
        List<List<Integer>> destinationsList = new ArrayList<List<Integer>>() {
            {
                add(new ArrayList<Integer>() {
                    {
                        add(1);
                        add(1);
                    }
                });
                add(new ArrayList<Integer>() {
                    {
                        add(2);
                        add(3);
                    }
                });
                add(new ArrayList<Integer>() {
                    {
                        add(1);
                        add(5);
                    }
                });
                add(new ArrayList<Integer>() {
                    {
                        add(3);
                        add(6);
                    }
                });
                add(new ArrayList<Integer>() {
                    {
                        add(1);
                        add(8);
                    }
                });
                add(new ArrayList<Integer>() {
                    {
                        add(2);
                        add(7);
                    }
                });
                add(new ArrayList<Integer>() {
                    {
                        add(5);
                        add(2);
                    }
                });
            }
        };
        int numberOfDestinations = destinationsList.size();
        int numberOfDeliveries = 3;
        List<List<Integer>> finalDeliveries = findNearestMinimumDeliveries(numberOfDestinations, destinationsList, numberOfDeliveries);
        for (List<Integer> list : finalDeliveries) {
            System.out.print(list);
        }
    }

    private static List<List<Integer>> findNearestMinimumDeliveries(int numberOfDestinations, List<List<Integer>> destinationsList, int numberOfDeliveries) {
        Map<List<Integer>, Double> hashMap = new HashMap<List<Integer>, Double>();
        for (int i = 0; i < numberOfDestinations; i++) {
            Integer x = destinationsList.get(i).get(0);
            Integer y = destinationsList.get(i).get(1);
            Double distance = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
            hashMap.put(destinationsList.get(i), distance);
        }
        return sortAndReturnMinimumDeliveries(hashMap, numberOfDeliveries);
    }

    private static List<List<Integer>> sortAndReturnMinimumDeliveries(Map<List<Integer>, Double> hashMap,
                                                                      int numberOfDeliveries) {
        AtomicInteger numDeliveries = new AtomicInteger(numberOfDeliveries);
        Map<List<Integer>, Double> sortedMap = hashMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue()).collect(toMap(Map.Entry::getKey,
                        Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

        List<List<Integer>> finalResult = new ArrayList<>();
        sortedMap.keySet().stream().forEach( key -> {
            if (numDeliveries.getAndDecrement() > 0) {
                finalResult.add(key);
            }
        });
        return finalResult;
    }
}