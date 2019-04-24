package leetcode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class GenerateParantheses {
    // Function that print all combinations of
    // balanced parentheses
    // open store the count of opening parenthesis
    // close store the count of closing parenthesis
    static void _printParenthesis(char str[], int pos, int n, int open, int close, ArrayList<String> resultList)
    {
        if(close == n)
        {
            StringBuilder sb = new StringBuilder();
            // print the possible combinations
            for(int i=0;i<str.length;i++)
                sb.append(str[i]);
            resultList.add(sb.toString());
            return;
        }
        else
        {
            if(open > close) {
                str[pos] = '}';
                _printParenthesis(str, pos+1, n, open, close+1, resultList);
            }
            if(open < n) {
                str[pos] = '{';
                _printParenthesis(str, pos+1, n, open+1, close, resultList);
            }
        }
    }

    // Wrapper over _printParenthesis()
    static List<String> printParenthesis(char str[], int n)
    {
        ArrayList<String> resultList = new ArrayList<>();
        if(n > 0)
            _printParenthesis(str, 0, n, 0, 0, resultList);
        return resultList;
    }

    // driver program
    public static void main (String[] args)
    {
        int n = 3;
        char[] str = new char[2 * n];
        printParenthesis(str, n);
    }
}
