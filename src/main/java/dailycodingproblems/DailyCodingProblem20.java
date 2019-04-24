/**
 * Given two singly linked lists that intersect at some point, find the intersecting node.
 * The lists are non-cyclical.
 *
 * For example, given A = 3 -> 7 -> 8 -> 10 and B = 99 -> 1 -> 8 -> 10, return the node with value 8.
 *
 * In this example, assume nodes with the same value are the exact same node objects.
 *
 * Do this in O(M + N) time (where M and N are the lengths of the lists) and constant space.
 */
package dailycodingproblems;

import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

@Getter
@Setter
class LinkedListNode {
    int data;
    LinkedListNode next;

    public LinkedListNode(int data, LinkedListNode next) {
        this.data = data;
        this.next = next;
    }
}
public class DailyCodingProblem20 {
    public static void main(String[] args) throws IOException {
        LinkedListNode rootA = new LinkedListNode(3,
                        new LinkedListNode(7,
                        new LinkedListNode( 11,
                        new LinkedListNode(9,
                        new LinkedListNode(8,
                        new LinkedListNode(2, null))))));
        LinkedListNode rootB = new LinkedListNode(99,
                        new LinkedListNode(1,
                        new LinkedListNode(8,
                        new LinkedListNode(2,null))));
        int result = findIntersectingNode(rootA, rootB);
        System.out.println("Intersecting Node :"+result);
    }

    private static int findIntersectingNode(LinkedListNode rootA, LinkedListNode rootB) {
        LinkedListNode nexTA = rootA;
        LinkedListNode nextB = rootB;
        int countA = 0;
        int countB = 0;
        if(rootA == null || rootB == null) {
            return 0;
        }
        while(nexTA != null) {
            countA++;
            nexTA = nexTA.getNext();
        }

        while(nextB != null) {
            countB++;
            nextB = nextB.getNext();
        }

        int diff;
        LinkedListNode longList;
        LinkedListNode shortList;
        if(countA > countB) {
            diff = countA - countB;
            longList = rootA;
            shortList = rootB;
        } else {
            diff = countB - countA;
            longList = rootB;
            shortList = rootA;
        }

        while(longList != null && longList.getNext() != null && diff != 0) {
            longList = longList.getNext();
            diff--;
        }

        while(longList != null && shortList != null) {
            if(longList.getData() == shortList.getData()) {
                return longList.getData();
            } else {
                longList = longList.getNext();
                shortList = shortList.getNext();
            }
        }
        return 0;
    }
}
