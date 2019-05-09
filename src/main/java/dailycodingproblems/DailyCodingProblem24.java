/**
 * Implement locking in a binary tree. A binary tree node can be locked or unlocked only if all of its descendants
 * or ancestors are not locked.
 *
 * Design a binary tree node class with the following methods:
 *
 * 1. is_locked, which returns whether the node is locked
 * 2. lock, which attempts to lock the node. If it cannot be locked, then it should return false.
 *    Otherwise, it should lock it and return true.
 * 3. unlock, which unlocks the node. If it cannot be unlocked, then it should return false.
 *    Otherwise, it should unlock it and return true.
 *
 * You may augment the node to add parent pointers or any other property you would like.
 * You may assume the class is used in a single-threaded program, so there is no need for actual locks or mutexes.
 * Each method should run in O(h), where h is the height of the tree.
 */

package dailycodingproblems;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
class TreeNode {
    TreeNode left;
    TreeNode right;
    TreeNode parent;
    boolean lock;
    int countOfLockedDescendants;

    public TreeNode(){
        this.left = null;
        this.right = null;
        this.lock = false;
        this.countOfLockedDescendants = 0;
    }

    public TreeNode(TreeNode left, TreeNode right, TreeNode parent, boolean isLocked) {
        this.left = left;
        this.right = right;
        this.parent = parent;
        this.lock = isLocked;
        this.countOfLockedDescendants = 0;
    }

    public boolean isLocked(TreeNode node) {
        return node.isLock();
    }

    public boolean canBeLockedOrUnlocked() {
        if (this.getCountOfLockedDescendants() > 0) {
            return false;
        }
        TreeNode current = this.getParent();
        while(current != null) {
            if(current.isLock()) {
                return false;
            }
            current = current.getParent();
        }
        return true;
    }

    public boolean lock() {
        if(canBeLockedOrUnlocked()) {
            this.setLock(true);
            TreeNode current = this.getParent();
            while(current != null) {
                current.setCountOfLockedDescendants(current.getCountOfLockedDescendants()+1);
                current = current.getParent();
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean unlock() {
        if(canBeLockedOrUnlocked()) {
            this.setLock(false);
            TreeNode current = this.getParent();
            while(current != null) {
                current.setCountOfLockedDescendants(current.getCountOfLockedDescendants()-1);
                current = current.getParent();
            }
            return true;
        } else {
            return false;
        }
    }
}
public class DailyCodingProblem24 {
    public static void main(String[] args) {
    }
}
