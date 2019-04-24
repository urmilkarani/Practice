/**
 * There exists a staircase with N steps, and you can climb up either 1 or 2 steps at a time.
 * Given N, write a function that returns the number of unique ways you can climb the staircase.
 * The order of the steps matters.
 *
 * For example, if N is 4, then there are 5 unique ways:
 *
 * 1, 1, 1, 1
 * 2, 1, 1
 * 1, 2, 1
 * 1, 1, 2
 * 2, 2
 * What if, instead of being able to climb 1 or 2 steps at a time, you could climb any number from a set
 * of positive integers X? For example, if X = {1, 3, 5}, you could climb 1, 3, or 5 steps at a time.
 */
package dailycodingproblems;

public class DailyCodingProblem12 {
    public static void main (String[] args) {
        int[] X = {1,2};
        int N = 5;
        int[] count = new int[N+1];
        count[0] = 1;
        int ways = findAllWays(N,X,count);
        System.out.println(ways);
    }

    private static int findAllWays(int n, int[] x, int[] count) {
        if(n == 0) {
            return 1;
        } else if (n < 0) {
            return 0;
        } else {
            for(int i = 0 ; i <= n; i++) {
                for(int elem : x) {
                    if(i-elem > 0) {
                        count[i] = count[i] + count[i-elem];
                    }
                    if (i == elem) {
                        count[i] = count[i] + 1;
                    }
                }
            }
        }
        return count[count.length-1];
    }
}
