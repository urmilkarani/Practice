package org.example.neetcode;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Arrays;

public class TopKFrequentElements {
    public int[] topKFrequent(int[] numbers, int k) {
        Map<Integer, Integer> frequencies = new HashMap<>();
        for (int number : numbers) frequencies.put(number, frequencies.getOrDefault(number, 0) + 1);

        PriorityQueue<int[]> minimumHeap = new PriorityQueue<>((first, second) -> first[0] - second[0]);
        for (Map.Entry<Integer, Integer> entry : frequencies.entrySet()) {
            minimumHeap.offer(new int[]{entry.getValue(), entry.getKey()});
            if (minimumHeap.size() > k) minimumHeap.poll();
        }
        int[] result = new int[k];
        for (int index = 0; index < k; index++) result[index] = minimumHeap.poll()[1];
        return result;
    }

    public static void main(String[] args) {
        System.out.println(Arrays.toString(new TopKFrequentElements().topKFrequent(new int[]{1, 2, 2, 3, 3, 3}, 2)));
    }
}
