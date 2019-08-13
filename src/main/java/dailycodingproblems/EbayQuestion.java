/**
 * given an encoded string S which only contains [a-zA-Z0-9], decode the string as following:
 *
 * If the character read is a letter, the letter is written to the buffer.
 * If the character read is a digit (say d), the entire buffer is repeated d-1 times in total.
 *
 * Now for the encoded string S, and an index K, find and return the K-th letter (1 indexed) in the decoded string
 *
 * example 1
 * s = abc2test2ok
 * k = 12
 * the result will be 'b'
 * explain: the entire buffer is abcabctestabcabctestok
 *
 * example 2
 * s = a10t2ok
 * k = 12
 * the result will be 'a'
 * explain: the entire buffer is aaaaaaaaaataaaaaaaaaatok
 */
package dailycodingproblems;

public class EbayQuestion {
    public static void main (String[] args) {
        String s = "a10t2ok";
        String s1 = "abc2test2ok";
        int k = 12;
        String result = decodeAtIndex(s1,k);
        System.out.println("Result: "+ result);
    }

    public static String decodeAtIndex(String S, int K) {
        if(S.isEmpty() && K > 0) {
            return null;
        }
        StringBuilder str = new StringBuilder();
        StringBuilder numberStr = new StringBuilder();
        boolean isPreviousCharacterDigit = false;
        char[] charArr = S.toCharArray();
        int length = S.length();
        for (int i = 0; i < length; i++) {
            if (!Character.isDigit(charArr[i])) {
                if(isPreviousCharacterDigit) {
                    int number = Integer.parseInt(numberStr.toString());
                    String tempString = str.toString();
                    for(int j = 0; j < number-1; j++) {
                        str.append(tempString);
                    }
                }
                isPreviousCharacterDigit = false;
                str.append(charArr[i]);
                numberStr = new StringBuilder();
            } else {
                isPreviousCharacterDigit = true;
                numberStr.append(charArr[i]);
            }
        }
        char[] finalCharArr = str.toString().toCharArray();
        return String.valueOf(finalCharArr[K-1]);

    }
}
