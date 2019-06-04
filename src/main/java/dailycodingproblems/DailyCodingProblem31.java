/**
 * The edit distance between two strings refers to the minimum number of character insertions, deletions,
 * and substitutions required to change one string to the other.
 * For example, the edit distance between “kitten” and “sitting” is three: substitute the “k” for “s”,
 * substitute the “e” for “i”, and append a “g”.
 *
 * Given two strings, compute the edit distance between them.
 */
package dailycodingproblems;

public class DailyCodingProblem31 {
    public static void main(String[] args) {
        String compareOne = "kitton";
        String compareTwo = "abcdef";
        int result = findEditDistance(compareOne, compareTwo);
        System.out.println("Edit Distance: " + result);
    }

    private static int findEditDistance(String compareOne, String compareTwo) {
        if(compareOne.isEmpty() || compareTwo.isEmpty()) {
            return compareOne.isEmpty() ? compareTwo.length() : compareOne.length();
        }
        int[][] table = new int[compareOne.length()+1][compareTwo.length()+1];
        int rowLength = compareOne.length();
        int columnlength = compareTwo.length();
        while(rowLength >= 0){
            table[rowLength][0] = rowLength;
            rowLength--;
        }
        while(columnlength >= 0){
            table[0][columnlength] = columnlength;
            columnlength--;
        }
        for(int i = 1 ; i <= compareOne.length() ; i++) {
            for(int j = 1 ; j <= compareTwo.length(); j++) {
                if(compareOne.charAt(i-1) != compareTwo.charAt(j-1)) {
                    int tempMin = Math.min(table[i][j-1], table[i-1][j]);
                    table[i][j] = Math.min(tempMin, table[i-1][j-1]) + 1;
                } else {
                    table[i][j] = table[i-1][j-1];
                }
            }
        }
        return table[compareOne.length()][compareTwo.length()];
    }
}
