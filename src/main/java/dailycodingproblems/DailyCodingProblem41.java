/**
 * Given an unordered list of flights taken by someone, each represented as (origin, destination) pairs, and a starting
 * airport, compute the person's itinerary. If no such itinerary exists, return null. If there are multiple possible
 * itineraries, return the lexicographically smallest one. All flights must be used in the itinerary.
 *
 * For example, given the list of flights [('SFO', 'HKO'), ('YYZ', 'SFO'), ('YUL', 'YYZ'), ('HKO', 'ORD')] and starting
 * airport 'YUL', you should return the list ['YUL', 'YYZ', 'SFO', 'HKO', 'ORD'].
 *
 * Given the list of flights [('SFO', 'COM'), ('COM', 'YYZ')] and starting airport 'COM', you should return null.
 *
 * Given the list of flights [('A', 'B'), ('A', 'C'), ('B', 'C'), ('C', 'A')] and starting airport 'A', you should
 * return the list ['A', 'B', 'C', 'A', 'C'] even though ['A', 'C', 'A', 'B', 'C'] is also a valid itinerary. However,
 * the first one is lexicographically smaller.
 */
package dailycodingproblems;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class DailyCodingProblem41 {
    static HashMap<String, PriorityQueue<String>> hashMap = new HashMap<>();
    static LinkedList<String> result = new LinkedList<>();
    public static void main(String[] args) {
        String[][] itinArray = {
                {"SFO","HKO"},
                {"COM","XYZ"}
        };
        String start = "COM";
        List<String> result = findItineraries(itinArray, start);
        System.out.println("Itins: "+ Arrays.toString(result.toArray()));
    }

    private static List<String> findItineraries(String[][] itinArray, String start) {
        for(String[] strings : itinArray) {
            if(!hashMap.containsKey(strings[0])) {
                PriorityQueue<String> currentQueue = new PriorityQueue<>();
                hashMap.put(strings[0],currentQueue);
            }
            hashMap.get(strings[0]).offer(strings[1]);
        }
        dfs(start);
        return result;
    }

    private static void dfs(String start) {
        PriorityQueue<String> queue = hashMap.get(start);
        while(queue != null && !queue.isEmpty()) {
            dfs(queue.poll());
        }
        result.offerFirst(start);
    }
}
