/*
Suppose we have some input data describing a graph of relationships between parents and children over multiple generations. The data is formatted as a list of (parent, child) pairs, where each individual is assigned a unique integer identifier.

For example, in this diagram, 3 is a child of 1 and 2, and 5 is a child of 4:

1   2   4
 \ /   / \
  3   5   8
   \ / \   \
    6   7   10

Write a function that takes this data as input and returns two collections: one containing all
individuals with zero known parents, and one containing all individuals with exactly one known parent.

Sample input/output (pseudodata):

int[][] parentChildPairs = new int[][] {
    {1, 3}, {2, 3}, {3, 6}, {5, 6}, {5, 7},
    {4, 5}, {4, 8}, {8, 10}
};

3 > {1,2}
6 > {3,5}

findNodesWithZeroAndOneParents(parentChildPairs) => [
  [1, 2, 4],    // Individuals with zero parents
  [5, 7, 8, 10] // Individuals with exactly one parent
]

*/

package dailycodingproblems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataBricksQuestion {
    public static void main(String[] args) {
        int[][] parentChildPairs = new int[][] {
                {1, 3}, {2, 3}, {3, 6}, {5, 6}, {5, 7},
                {4, 5}, {4, 8}, {8, 10}};
        List<List<Integer>> finalResult = findParentChildRelationships(parentChildPairs);
        System.out.println("Nodes with no parent: ");
        printElements(finalResult.get(0));
        System.out.println("Nodes with one parent: ");
        printElements(finalResult.get(1));
    }

    private static List<List<Integer>> findParentChildRelationships(int[][] parentChildPairs) {
        int rowLength = parentChildPairs.length;
        int columnLength = parentChildPairs[0].length;
        HashMap<Integer, List<Integer>> relationMap = new HashMap<>();
        List<Integer> noParentList = new ArrayList<>();
        List<Integer> oneParentList = new ArrayList<>();
        List<List<Integer>> resultList = new ArrayList<>();
        for(int i = 0; i < rowLength; i++) {
            int[] currentPair = parentChildPairs[i];
            int parent = currentPair[0];
            int child = currentPair[1];
            if(relationMap.containsKey(child)) {
                List<Integer> currentList = relationMap.get(child);
                currentList.add(parent);
                relationMap.put(child, currentList);
            } else {
                List<Integer> newList = new ArrayList<Integer>();
                newList.add(parent);
                relationMap.put(child, newList);
            }
        }

        for(int i = 0; i < rowLength; i++) {
            int[] currentPair = parentChildPairs[i];
            int child = currentPair[1];
            System.out.println(!relationMap.containsKey(child));
            if(!relationMap.containsKey(child)) {
                noParentList.add(child);
                printElements(noParentList);
            }
        }

        for (Map.Entry<Integer, List<Integer>> entry : relationMap.entrySet())     {
            List<Integer> parentList = entry.getValue();
            System.out.println("parentList");
            if(parentList.size() == 1) {
                oneParentList.add(entry.getKey());
            }
        }
        resultList.add(noParentList);
        resultList.add(oneParentList);
        return resultList;
    }

    private static void printElements(List<Integer> input) {
        for(Integer element : input) {
            System.out.print(element+" ");
        }
        System.out.println();
    }

}
