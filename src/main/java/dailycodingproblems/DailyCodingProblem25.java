/**
 * Implement regular expression matching with the following special characters:
 * . (period) which matches any single character
 * * (asterisk) which matches zero or more of the preceding element
 * That is, implement a function that takes in a string and a valid regular expression and returns
 * whether or not the string matches the regular expression.
 *
 * For example, given the regular expression "ra." and the string "ray", your function should return true.
 * The same regular expression on the string "raymond" should return false.
 *
 * Given the regular expression ".*at" and the string "chat", your function should return true.
 * The same regular expression on the string "chats" should return false.
 */
package dailycodingproblems;

public class DailyCodingProblem25 {
    public static void main(String[] args) {
        String str = "rammy";
        String regex1 = "ra.";
        String regex2 = "*y";
        String regex3 = "*.";
        String regex4 = "ra*y";
        System.out.println(matchExpression(regex1,str));
        System.out.println(matchExpression(regex2,str));
        System.out.println(matchExpression(regex3,str));
        System.out.println(matchExpression(regex4,str));
        System.out.println(matchExpression("",str));
        System.out.println(matchExpression(regex1,""));
    }

    private static boolean matchExpression(String regex, String str) {
        if(regex.length() == 0 && str.length() == 0) {
            return true;
        }

        if(regex.length() > 1 && regex.charAt(0)=='*' && str.length() == 0) {
            return false;
        }

        if((regex.length() >= 1 && regex.charAt(0)=='.') || (regex.length() != 0 && str.length() != 0
            && regex.charAt(0) == str.charAt(0))) {
            return matchExpression(regex.substring(1),str.substring(1));
        }

        if(regex.length() != 0 && regex.charAt(0) == '*') {
            return matchExpression(regex.substring(1),str) || matchExpression(regex,str.substring(1));
        }
        return false;
    }
}

