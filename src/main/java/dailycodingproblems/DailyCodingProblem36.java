/**
 * Given the root to a binary search tree, find the second largest node in the tree.
 */

package dailycodingproblems;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
class TreeNodeInt {
    private int val;
    TreeNodeInt leftNode;
    TreeNodeInt rightNode;

    public TreeNodeInt(){}

    public TreeNodeInt(int val, TreeNodeInt leftNode, TreeNodeInt rightNode){
        this.val = val;
        this.leftNode = leftNode;
        this.rightNode = rightNode;
    }
    public TreeNodeInt(int val){
        this.val = val;
        this.leftNode = null;
        this.rightNode = null;
    }
}
public class DailyCodingProblem36 {
    private static int count;
    public static void main(String args[]) {
        TreeNodeInt root = new TreeNodeInt(6,
                new TreeNodeInt(4,new TreeNodeInt(2,new TreeNodeInt(0),new TreeNodeInt(3)),
                        new TreeNodeInt(5,new TreeNodeInt(4),new TreeNodeInt(5))),null);
        int k = 2;
        int result = findKthLargestNode(root,k);
        System.out.println("Element: "+ result);
    }

    private static int findKthLargestNode(TreeNodeInt root, int k) {
        if(root == null) {
            return 0;
        }
        int node = findKthLargestNode(root.getRightNode(), k);
        if(count != k) {
            count++;
            node = root.getVal();
        }
        if(count == k) {
            return node;
        } else {
            return findKthLargestNode(root.getLeftNode(), k);
        }
    }
}
