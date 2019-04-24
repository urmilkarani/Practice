/**
 * Given the root to a binary tree, implement serialize(root), which serializes the tree into a string,
 * and deserialize(s), which deserializes the string back into the tree.
 *
 * For example, given the following Node class
 *
 * class Node:
 *     def __init__(self, val, left=None, right=None):
 *         self.val = val
 *         self.left = left
 *         self.right = right
 * The following test should pass:
 *
 * node = Node('root', Node('left', Node('left.left')), Node('right'))
 * assert deserialize(serialize(node)).left.left.val == 'left.left'
 */

package dailycodingproblems;

class Node {
    private String val;
    private Node left;
    private Node right;

    public Node(String val, Node left, Node right) {
        this.val = val;
        this.left = left;
        this.right = right;
    }

    public Node() {
    }

    public String getVal() {
        return this.val;
    }

    public Node getLeftNode() {
        return this.left;
    }

    public Node getRightNode() {
        return this.right;
    }

    public void setVal(String value) {
        val = value;
    }

    public void setLeftNode(Node leftNode) {
        left = leftNode;
    }

    public void setRightNode(Node rightNode) {
        right = rightNode;
    }
}

public class DailyCodingProblem3 {

    public static void main(String[] args) {
        Node root = new Node("root",
                new Node("left", new Node("left.left",null,null),null),
                new Node("right", new Node("right.left",null,null), null));
        String str = serialize(root);
        System.out.println(str);
        Node tree = deserialize(str);
        System.out.println(tree.getRightNode().getLeftNode().getVal());
    }

    private static Node deserialize(String str) {
        String[] splitList = str.split("\\|");
        int[] index = {0};
        Node root = deserialize(splitList,index);
        return root;
    }

    private static Node deserialize(String[] splitList, int[] index) {
        if(splitList[index[0]].equalsIgnoreCase("null")) {
            return null;
        }
        Node node = new Node(splitList[index[0]], null, null);
        index[0]++;
        node.setLeftNode(deserialize(splitList,index));
        index[0]++;
        node.setRightNode(deserialize(splitList,index));
        return node;
    }


    private static String serialize(Node root) {
        StringBuilder stringBuilder = new StringBuilder();
        while(root != null) {
            stringBuilder.append(root.getVal()).append("|");
            if(root.getLeftNode() != null) {
                stringBuilder.append(serialize(root.getLeftNode()));
            } else {
                stringBuilder.append("null").append("|");
            }
            if(root.getRightNode() != null) {
                stringBuilder.append(serialize(root.getRightNode()));
            } else {
                stringBuilder.append("null").append("|");
            }
            break;
        }

        return stringBuilder.toString();
    }
}
