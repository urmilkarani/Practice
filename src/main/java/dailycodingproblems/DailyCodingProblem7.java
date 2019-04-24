/**
 * Given the mapping a = 1, b = 2, ... z = 26, and an encoded message, count the number of ways it can be decoded.
 *
 * For example, the message '1111' would give 3, since it could be decoded as 'aaa', 'ka', and 'ak'.
 *
 * You can assume that the messages are decodable. For example, '001' is not allowed.
 */
package dailycodingproblems;

public class DailyCodingProblem7 {

    public static void main(String[] args) {
        String valueToEncode = "010101";
        int numberOfPermutations = numberOfDecodedString(valueToEncode.toCharArray());
        System.out.println(numberOfPermutations);
    }

    private static int numberOfDecodedString(char[] valueToEncode) {
        int[] count = new int[valueToEncode.length+1];
        count[0] = 1;
        count[1] = 1;

        for(int i = 2; i<=valueToEncode.length; i++) {
            count[i] = 0;
            if(valueToEncode[i-1] > '0') {
                count[i] = count[i-1];
            }

            if(valueToEncode[i-2] == '1' || (valueToEncode[i-2] == '2' && valueToEncode[i-1] < '7')) {
                count[i] += count[i-2];
            }
        }
        return count[valueToEncode.length];
    }
}
