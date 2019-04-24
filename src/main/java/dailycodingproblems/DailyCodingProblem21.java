/**
 * Given an array of time intervals (start, end) for classroom lectures (possibly overlapping),
 * find the minimum number of rooms required.
 *
 * For example, given [(30, 75), (0, 50), (60, 150)], you should return 2.
 */
package dailycodingproblems;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class DailyCodingProblem21 {
    public static void main(String[] args) {
        int[][] lectures = {{30,75},{0,50},{80,100},{120,150},{100,150}};
        int result = findMinimumNumberOfRooms(lectures);
        System.out.println("Minimun Rooms: "+result);
    }

    private static int findMinimumNumberOfRooms(int[][] lectures) {
        int count = 1;
        if(lectures == null) {
            return 0;
        }
        Arrays.sort(lectures, Comparator.comparingInt(o -> o[0]));
        LinkedList<int[]> queue = new LinkedList<>();
        queue.offer(lectures[0]);
        for(int i = 1; i < lectures.length; i++) {
            int[] current = queue.peek();
            if(lectures[i][0] < current[1]) {
                count++;
            } else {
                queue.poll();
            }
            queue.offer(lectures[i]);

        }
        return count;
    }
}
