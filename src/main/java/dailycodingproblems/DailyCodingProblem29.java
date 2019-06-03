/**
 * Run-length encoding is a fast and simple method of encoding strings.
 * The basic idea is to represent repeated successive characters as a single count and character.
 * For example, the string "AAAABBBCCDAA" would be encoded as "4A3B2C1D2A".
 *
 * Implement run-length encoding and decoding.
 * You can assume the string to be encoded have no digits and consists solely of alphabetic characters.
 * You can assume the string to be decoded is valid.
 */

package dailycodingproblems;

import com.sun.deploy.util.StringUtils;

public class DailyCodingProblem29 {
    public static void main(String[] args) {
        String input = "AAAABBBCCDAA";
        String encodedString = encodeInput(input);
        System.out.println("Encoded String : " + encodedString);
        String decodedString = decodeInput(encodedString);
        System.out.println("Decoded String : " + decodedString);
    }

    private static String decodeInput(String encodedString) {
        StringBuilder sb = new StringBuilder();
        int i = 0, j =1;
        while(i < encodedString.length() && j < encodedString.length()) {
            int count = Integer.parseInt(encodedString.substring(i,j));
            while(count != 0) {
                sb.append(encodedString.charAt(j));
                count--;
            }
            i+=2;
            j+=2;
        }
        return sb.toString();
    }

    private static String encodeInput(String input) {
        StringBuffer sb = new StringBuffer();
        int count = 1;
        for(int i = 0; i < input.length(); i++) {
            if( i+1 == input.length() || input.charAt(i) != input.charAt(i+1)) {
                sb.append(count).append(input.charAt(i));
                count = 1;
            } else {
                count++;
            }
        }
        return sb.toString();
    }
}
