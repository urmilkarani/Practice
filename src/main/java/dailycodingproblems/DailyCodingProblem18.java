/**
 * Given an array of integers and a number k, where 1 <= k <= length of the array,
 * compute the maximum values of each subarray of length k.
 *
 * For example, given array = [10, 5, 2, 7, 8, 7] and k = 3, we should get: [10, 7, 8, 8], since:
 *
 * 10 = max(10, 5, 2)
 * 7 = max(5, 2, 7)
 * 8 = max(2, 7, 8)
 * 8 = max(7, 8, 7)
 */
package dailycodingproblems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;

public class DailyCodingProblem18 {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        String[] inputString = bufferedReader.readLine().split(" ");
        int k = Integer.parseInt(bufferedReader.readLine());
        int[] numbers = new int[inputString.length];
        int i = 0;
        for (String str: inputString) {
            numbers[i] = Integer.parseInt(str);
            i++;
        }
        printMaximumOfSubArrays(numbers,k);
    }

    private static void printMaximumOfSubArrays(int[] numbers, int k) {
        int length = numbers.length;
        LinkedList<Integer> deque = new LinkedList<>();
        for(int i = 0; i<length; i++) {
            if (!deque.isEmpty() && deque.peekFirst() == i-k) {
                 deque.poll();
            }
            while (!deque.isEmpty() && numbers[deque.peekLast()] < numbers[i]) {
                deque.pollLast();
            }

            deque.offer(i);
            if(i+1>=k){
                System.out.print(numbers[deque.peek()]+" ");
            }
        }
    }
}
