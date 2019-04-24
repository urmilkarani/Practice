/**
 * Suppose we represent our file system by a string in the following manner:
 * The string "dir\n\tsubdir1\n\tsubdir2\n\t\tfile.ext" represents:
 * dir
 *     subdir1
 *     subdir2
 *         file.ext
 * The directory dir contains an empty sub-directory subdir1 and a sub-directory subdir2 containing a file file.ext.
 * The string "dir\n\tsubdir1\n\t\tfile1.ext\n\t\tsubsubdir1\n\tsubdir2\n\t\tsubsubdir2\n\t\t\tfile2.ext"
 * represents:
 * dir
 *     subdir1
 *         file1.ext
 *         subsubdir1
 *     subdir2
 *         subsubdir2
 *             file2.ext
 * The directory dir contains two sub-directories subdir1 and subdir2.
 * subdir1 contains a file file1.ext and an empty second-level sub-directory subsubdir1.
 * subdir2 contains a second-level sub-directory subsubdir2 containing a file file2.ext.
 *
 * We are interested in finding the longest (number of characters) absolute path to a file within our file system.
 * For example, in the second example above, the longest absolute path is "dir/subdir2/subsubdir2/file2.ext",
 * and its length is 32 (not including the double quotes).
 *
 * Given a string representing the file system in the above format, return the length of the longest absolute path
 * to a file in the abstracted file system. If there is no file in the system, return 0.
 */
package dailycodingproblems;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Stack;

@Getter
@Setter
class DirectoryNode {
    int length;
    int level;

    public DirectoryNode(int length, int level) {
        this.length = length;
        this.level= level;
    }
}

public class DailyCodingProblem17 {
    public static void main(String[] args) {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            String directoryString = bufferedReader.readLine();
            int result = findLongestAbsolutePath(directoryString);
            System.out.println(result);
        } catch (IOException ex) {
            System.out.println("IO Exception occured : " +ex.getMessage());
        }
    }

    private static int findLongestAbsolutePath(String directoryString) {
        int max = 0;
        if(!directoryString.isEmpty()) {
            Stack<DirectoryNode> stack = new Stack<>();
            String[] splitDirectoryString = directoryString.split("\\\\n");
            for(int i = 0; i < splitDirectoryString.length; i++) {
                String curr = splitDirectoryString[i];
                int count = 0;
                int j = 0;
                while(j < curr.length()-1){
                    if("\\t".equals(curr.substring(j,j+2))) {
                        j+=2;
                        count++;
                    } else {
                        break;
                    }
                }

                while(!stack.isEmpty() && count <=stack.peek().level) {
                    stack.pop();
                }
                if(curr.contains(".")) {
                    if(stack.isEmpty()) {
                        max = Math.max(curr.length() - (count*2), max);
                    } else {
                        max = Math.max(stack.peek().length + curr.length() - (count*2), max);
                    }
                } else {
                    if(stack.isEmpty()) {
                        stack.push(new DirectoryNode(curr.length() - (count*2) + 1, count));
                    } else {
                        stack.push(new DirectoryNode(stack.peek().length + curr.length() - (count*2) + 1,
                                count));
                    }
                }
            }
        }
        return max;
    }
}
