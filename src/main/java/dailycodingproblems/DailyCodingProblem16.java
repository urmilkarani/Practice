/**
 * You run an e-commerce website and want to record the last N order ids in a log.
 * Implement a data structure to accomplish this, with the following API:
 *
 * record(order_id): adds the order_id to the log
 * get_last(i): gets the ith last element from the log. i is guaranteed to be smaller than or equal to N.
 * You should be as efficient with time and space as possible.
 */
package dailycodingproblems;

class LogDataStructure {
    private int maxSize;
    private int[] circularBuffer;
    private int currentIndex;

    public LogDataStructure(int maxSize) {
        this.maxSize = maxSize;
        this.circularBuffer = new int[maxSize];
        this.currentIndex = 0;
    }

    public void record(int orderId) {
        circularBuffer[currentIndex] = orderId;
        currentIndex = (currentIndex+1) % maxSize;
    }

    public int getLast(int i) {
        return circularBuffer[(currentIndex - i + maxSize)%maxSize];
    }
}
public class DailyCodingProblem16 {

    public static void main(String[] args) {
        LogDataStructure logDataStructure = new LogDataStructure(10);
        int i = 1;
        while(i <= 10) {
            logDataStructure.record(i);
            i++;
        }
        System.out.println("5th order id from last: "+logDataStructure.getLast(5));
        logDataStructure.record(11);
        logDataStructure.record(12);
        System.out.println("1st order id from last: " +logDataStructure.getLast(1));
        System.out.println("2nd order id from last: "+logDataStructure.getLast(2));
    }
}
