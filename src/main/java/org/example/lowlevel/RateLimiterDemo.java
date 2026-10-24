package org.example.lowlevel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RateLimiterDemo {

    // --- 1. Unified Rate Limiter Interface ---
    public interface RateLimiter {
        boolean tryAcquire();
    }

    // --- 2. Token Bucket Rate Limiter Policy ---
    public static class TokenBucketRateLimiter implements RateLimiter {
        private final long capacity;
        private final long refillRatePerSecond;         // Added field declaration
        private final double refillRatePerMillis;       // tokens per millisecond
        private double availableTokens;
        private long lastRefillTimestamp;
        private final Lock lock = new ReentrantLock();

        public TokenBucketRateLimiter(long capacity, long refillRatePerSecond) {
            this.capacity = capacity;
            this.refillRatePerSecond = refillRatePerSecond;
            this.availableTokens = capacity;
            this.refillRatePerMillis = (double) refillRatePerSecond / 1000.0;
            this.lastRefillTimestamp = System.currentTimeMillis();
        }

        @Override
        public boolean tryAcquire() {
            lock.lock();
            try {
                refill();
                if (availableTokens >= 1.0) {
                    availableTokens -= 1.0;
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }

        private void refill() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastRefillTimestamp;
            if (elapsed > 0) {
                double tokensToAdd = elapsed * refillRatePerMillis;
                availableTokens = Math.min(capacity, availableTokens + tokensToAdd);
                lastRefillTimestamp = now;
            }
        }
    }

    // --- 3. Fixed Window Counter Rate Limiter Policy ---
    // Limits requests to a fixed maximum count per calendar time window block.
    public static class FixedWindowRateLimiter implements RateLimiter {
        private final int limit;
        private final long windowSizeMillis;
        private long windowStartTimestamp;
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final Lock lock = new ReentrantLock();

        public FixedWindowRateLimiter(int limit, long windowSizeMillis) {
            this.limit = limit;
            this.windowSizeMillis = windowSizeMillis;
            this.windowStartTimestamp = System.currentTimeMillis();
        }

        @Override
        public boolean tryAcquire() {
            long now = System.currentTimeMillis();
            lock.lock();
            try {
                if (now - windowStartTimestamp >= windowSizeMillis) {
                    // Reset window
                    windowStartTimestamp = now;
                    requestCount.set(0);
                }

                if (requestCount.get() < limit) {
                    requestCount.incrementAndGet();
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }
    }

    // --- 4. Main Method Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Demonstrating Token Bucket Policy ===");
        // Capacity of 3 tokens, refilling at 2 tokens per second
        RateLimiter tokenBucket = new TokenBucketRateLimiter(3, 2);

        for (int i = 1; i <= 5; i++) {
            boolean allowed = tokenBucket.tryAcquire();
            System.out.println("Token Bucket Request " + i + " -> " + (allowed ? "ALLOWED[cite: 1]" : "THROTTLED"));
            Thread.sleep(100); // Fast bursts
        }

        System.out.println("\nWaiting 1.5 seconds for token replenishment...");
        Thread.sleep(1500);

        for (int i = 6; i <= 8; i++) {
            boolean allowed = tokenBucket.tryAcquire();
            System.out.println("Token Bucket Request " + i + " -> " + (allowed ? "ALLOWED[cite: 1]" : "THROTTLED"));
        }

        System.out.println("\n=== Demonstrating Fixed Window Counter Policy ===");
        // Max 3 requests per 1-second (1000ms) window
        RateLimiter fixedWindow = new FixedWindowRateLimiter(3, 1000);

        for (int i = 1; i <= 5; i++) {
            boolean allowed = fixedWindow.tryAcquire();
            System.out.println("Fixed Window Request " + i + " -> " + (allowed ? "ALLOWED[cite: 1]" : "THROTTLED"));
        }

        System.out.println("\nWaiting for the next fixed window block (1.1 seconds)...");
        Thread.sleep(1100);

        for (int i = 6; i <= 8; i++) {
            boolean allowed = fixedWindow.tryAcquire();
            System.out.println("Fixed Window Request " + i + " -> " + (allowed ? "ALLOWED[cite: 1]" : "THROTTLED"));
        }
    }
}
