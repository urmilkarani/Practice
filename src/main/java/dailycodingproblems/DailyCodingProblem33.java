/**
 * Compute the running median of a sequence of numbers. That is, given a stream of numbers,
 * print out the median of the list so far on each new element.
 *
 * Recall that the median of an even-numbered list is the average of the two middle numbers.
 *
 * For example, given the sequence [2, 1, 5, 7, 2, 0, 5], your algorithm should print out:
 *
 * 2 -> 1
 * 1.5 -> 1,2
 * 2 -> 1,2,5
 * 3.5 -> 1,2,5,7
 * 2 -> 1,2,2,5,7
 * 2 -> 0,1,2,2,5,7
 * 2 -> 0,1,2,2,5,5,7
 */
package dailycodingproblems;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DailyCodingProblem33 {
    public static void main(String[] args) {
        int[] input = {2, 1, 5, 7, 2, 0, 5};
        printRunningMedian(input);
    }

    private static void printRunningMedian(int[] input) {
        List<Integer> priorityQueue = new ArrayList<>(input.length);
        for(int i = 0; i < input.length; i++) {
            priorityQueue.add(input[i]);
            Collections.sort(priorityQueue);
            if(i == 0) {
                System.out.println(priorityQueue.get(i));
            } else if (i == 1) {
                System.out.println((double)(priorityQueue.get(0)+priorityQueue.get(1))/2);
            } else {
                if (i % 2 == 0) {
                    System.out.println(priorityQueue.get(priorityQueue.size()/2));
                } else{
                    int index = priorityQueue.size()/2;
                    System.out.println((double)(priorityQueue.get(index)+priorityQueue.get(index-1))/2);
                }
            }
        }
    }
}
