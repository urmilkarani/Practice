/**
 * We can determine how "out of order" an array A is by counting the number of inversions it has. Two elements A[i] and A[j]
 * form an inversion if A[i] > A[j] but i < j. That is, a smaller element appears after a larger element.
 *
 * Given an array, count the number of inversions it has. Do this faster than O(N^2) time.
 *
 * You may assume each element in the array is distinct.
 *
 * For example, a sorted list has zero inversions. The array [2, 4, 1, 3, 5] has three inversions: (2, 1), (4, 1), and (4, 3).
 * The array [5, 4, 3, 2, 1] has ten inversions: every distinct pair forms an inversion.
 */
package dailycodingproblems;


public class DailyCodingProblem44 {
    public static void main (String[] args) {
        int[] array = {2, 4, 1, 3, 5};
        //int[] array = {1, 2, 3, 4, 5};
        //int[] array = {5, 4, 3, 2, 1};
        int result = getInversions(array,0,array.length-1);
        //int result = getInversions(array);
        //int result = getInversions(array);
        System.out.println("Number of inversions: "+result);
    }

    private static int getInversions(int[] array, int start, int end) {
        int mid = (start + end)/2;
        if(start < end) {
            int leftInversion = getInversions(array,start,mid);
            int rightInversion = getInversions(array,mid+1,end);
            int sumInversion = mergeAndCount(array,start,mid,end);
            return leftInversion + rightInversion + sumInversion;
        }
        return 0;
    }

    private static int mergeAndCount(int[] array, int start, int mid, int end) {
        int[] temp = new int[end-start+1];
        int i = start, k = 0, count = 0;
        int j = mid+1;
        while(i <= mid && j <= end) {
            if(array[i] <= array[j]) {
                temp[k++] = array[i++];
            } else {
                count += mid - i + 1;
                temp[k++] = array[j++];
            }
        }
        while( i <= mid) {
            temp[k++] = array[i++];
        }
        while(j <= end) {
            temp[k++] = array[j++];
        }

        for(i = start; i < start + k; i++){
            array[i] = temp[i - start];
        }
        return count;
    }


}
