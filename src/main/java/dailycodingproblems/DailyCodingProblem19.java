/**
 * A builder is looking to build a row of N houses that can be of K different colors.
 * He has a goal of minimizing cost while ensuring that no two neighboring houses are of the same color.
 *
 * Given an N by K matrix where the nth row and kth column represents the cost to build the nth house with
 * kth color, return the minimum cost which achieves this goal.
 */
package dailycodingproblems;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class DailyCodingProblem19 {
    public static void main(String[] args) throws IOException {
        BufferedReader bufferedReader = new BufferedReader((new InputStreamReader(System.in)));
        int numberHouses = Integer.parseInt(bufferedReader.readLine());
        int numberPaint = Integer.parseInt(bufferedReader.readLine());
        int[][] paintCost = new int[numberHouses][numberPaint];
        for (int j = 0; j <numberHouses; j++) {
            String[] input = bufferedReader.readLine().split(" ");
            int k = 0;
            for(String str: input) {
                paintCost[j][k] = Integer.parseInt(str);
                k++;
            }
        }
        int minimumCost = calculateMinimumPaintingCost(numberHouses,numberPaint,paintCost);
        System.out.println("Cost :" + minimumCost);
    }

    private static int calculateMinimumPaintingCost(int numberHouses, int numberPaint, int[][] paintCost) {
        int minimumCost[] =new int[numberHouses];
        for(int i = 0; i < numberHouses; i++){
            minimumCost[i] = Integer.MAX_VALUE;
        }
        int[] housePaint = new int[numberHouses+1];
        for(int k = 0; k < numberPaint; k++) {
            if(paintCost[0][k] < minimumCost[0]) {
                minimumCost[0] = paintCost[0][k];
                housePaint[0] = k;
            }
        }
        for(int i = 1 ; i < numberHouses; i++) {
            int columnToAvoid = housePaint[i-1];
            for (int j = 0 ; j < paintCost[0].length; j++) {
                List<Integer> minimumList = findMinimum(paintCost, i, columnToAvoid);
                minimumCost[i] = minimumCost[i-1]+minimumList.get(0);
                housePaint[i] = minimumList.get(1);
            }
        }
        return minimumCost[numberHouses-1];
    }

    private static List<Integer> findMinimum(int[][] paintCost, int i, int j) {
        int row = i;
        int columnToAvoid = j;
        List<Integer> returnList = new ArrayList<>();
        returnList.add(Integer.MAX_VALUE);
        returnList.add(0);
        for (int k = 0; k < paintCost[0].length; k++) {
            if(k != columnToAvoid && paintCost[row][k] < returnList.get(0)) {
                returnList.set(0,paintCost[row][k]);
                returnList.set(1,k);
            }
        }
        return returnList;
    }
}
