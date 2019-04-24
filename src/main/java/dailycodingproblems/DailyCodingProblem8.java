/**
 * A unival tree (which stands for "universal value") is a tree where all nodes under it have the same value.
 * Given the root to a binary tree, count the number of unival subtrees.
 * For example, the following tree has 5 unival subtrees:
 *
 *       1
 *     /  \
 *    1    1
 *  / \   / \
 * 1  1  1   1
 *
 *
 */
package dailycodingproblems;

class Result {
    int count;
    boolean isUnival;

    public Result(int count, boolean isUnival){
        this.count = count;
        this.isUnival = isUnival;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setUnival(boolean unival) {
        isUnival = unival;
    }

    public boolean isUnival() {
        return isUnival;
    }
}
public class DailyCodingProblem8 {
    public static void main(String[] args) {
        Node root = new Node("1",new Node("1",new Node("1",null,null),
                new Node("1",null,null)),
                new Node("1",new Node("1",null,null),
                        new Node("1",null,null)));
        Result result = findUnivalTrees(root);
        System.out.println(result.getCount());
    }

    private static Result findUnivalTrees(Node root) {
        if (root == null) {
            return new Result(0,true);
        }
        Result left = findUnivalTrees(root.getLeftNode());
        Result right = findUnivalTrees(root.getRightNode());
        boolean isUniVal = true;
        if(!left.isUnival || !right.isUnival){
            isUniVal = false;
        }
        if(root.getLeftNode() != null && !root.getVal().equals(root.getLeftNode().getVal())) {
            isUniVal = false;
        }
        if(root.getRightNode() != null && !root.getVal().equals(root.getRightNode().getVal())) {
            isUniVal = false;
        }
        if(isUniVal) {
            return new Result(left.getCount() + right.getCount() + 1, true);
        } else {
            return new Result(left.getCount() + right.getCount(), false);
        }

    }
}
