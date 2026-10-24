package org.example.neetcode;

public class BuyAndSellCrypto {
    public int maxProfit(int[] prices) {
        if (prices == null || prices.length == 0) return 0;
        int result = 0;
        int left = 0;
        for (int right = 1; right < prices.length; right++) {
            if (prices[left] < prices[right]) result = Math.max(result, prices[right] - prices[left]);
            else left = right;
        }
        return result;
    }

    public static void main(String[] args) {
        System.out.println(new BuyAndSellCrypto().maxProfit(new int[]{10, 1, 5, 6, 7, 1}));
    }
}
