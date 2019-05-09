/**
 * Given a singly linked list and an integer k, remove the kth last element from the list.
 * k is guaranteed to be smaller than the length of the list.
 *
 * The list is very long, so making more than one pass is prohibitively expensive.
 *
 * Do this in constant space and in one pass.
 */
package dailycodingproblems;

public class DailyCodingProblem26 {
    public static void main(String[] args) {
        LinkedListNode root = new LinkedListNode(3,
                new LinkedListNode(7,
                new LinkedListNode( 11,
                new LinkedListNode(9,
                new LinkedListNode(8,
                new LinkedListNode(2,
                new LinkedListNode(7,
                new LinkedListNode(12,
                new LinkedListNode(23,
                new LinkedListNode(4,
                new LinkedListNode(6,
                new LinkedListNode(1,
                new LinkedListNode(5,
                new LinkedListNode(10,
                new LinkedListNode(0,null)))))))))))))));
        LinkedListNode result = removeKthLastElement(root,5);
        System.out.println(result.getData());
    }

    private static LinkedListNode removeKthLastElement(LinkedListNode root,int k) {
        int count = 0;
        LinkedListNode nodeK = root;
        LinkedListNode start = root;
            while(count < k) {
            nodeK = nodeK.getNext();
            count++;
        }

        while(nodeK != null) {
            nodeK = nodeK.getNext();
            start = start.getNext();
        }
        return start;
    }
}
