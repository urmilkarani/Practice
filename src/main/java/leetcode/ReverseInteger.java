package leetcode;

public class ReverseInteger {

    public static void main(String args[]) {
        int value = 1534236469;
        System.out.println(reverseInteger(value));

    }

    private static int reverseInteger(int value) {
        int temp = value;
        int result = 0;
        int finalResult = 0;
        if(value > Integer.MAX_VALUE || value < Integer.MIN_VALUE) {
            return 0;
        }
        while (temp != 0) {
            int tail = temp%10;
            result = result*10;
            result = result + tail;
            if((result - tail)/10 != finalResult) {
                return 0;
            }
            finalResult = result;
            temp = temp/10;
        }
        return finalResult;
    }
}
