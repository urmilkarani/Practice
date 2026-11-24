package org.example.lowlevel;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RateLimiterDemo {

    // --- 1. Unified Rate Limiter Interface ---
    public interface RateLimiter {
        boolean tryAcquire();
    }

    // --- 2. Token Bucket Rate Limiter Policy ---
    // Allows bursts of traffic while enforcing a stable average rate.
    public static class TokenBucketRateLimiter implements RateLimiter {
        private final long capacity;
        private final long refillRatePerSecond;
        private final double refillRatePerMillis;
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

    // --- 3. Leaky Bucket Rate Limiter Policy ---
    // Enforces a strict, smooth output rate by treating requests as water leaking at a constant rate.
    public static class LeakyBucketRateLimiter implements RateLimiter {
        private final long capacity; // Maximum water level (bucket capacity)
        private final double leakRatePerMillis; // Water leakage rate per millisecond
        private double waterLevel = 0.0;
        private long lastLeakTimestamp;
        private final Lock lock = new ReentrantLock();

        public LeakyBucketRateLimiter(long capacity, long leakRatePerSecond) {
            this.capacity = capacity;
            this.leakRatePerMillis = (double) leakRatePerSecond / 1000.0;
            this.lastLeakTimestamp = System.currentTimeMillis();
        }

        @Override
        public boolean tryAcquire() {
            lock.lock();
            try {
                leak();
                if (waterLevel + 1.0 <= capacity) {
                    waterLevel += 1.0;
                    return true;
                }
                return false; // Bucket overflow (throttled)
            } finally {
                lock.unlock();
            }
        }

        private void leak() {
            long now = System.currentTimeMillis();
            long elapsed = now - lastLeakTimestamp;
            if (elapsed > 0) {
                double leakedWater = elapsed * leakRatePerMillis;
                waterLevel = Math.max(0.0, waterLevel - leakedWater);
                lastLeakTimestamp = now;
            }
        }
    }

    // --- 4. Fixed Window Counter Rate Limiter Policy ---
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

    // --- 5. Sliding Window Log Rate Limiter Policy ---
    // Tracks individual request timestamps in a rolling window to eliminate boundary spikes.
    public static class SlidingWindowRateLimiter implements RateLimiter {
        private final int limit;
        private final long windowSizeMillis;
        private final Deque<Long> requestTimestamps = new ArrayDeque<>();
        private final Lock lock = new ReentrantLock();

        public SlidingWindowRateLimiter(int limit, long windowSizeMillis) {
            this.limit = limit;
            this.windowSizeMillis = windowSizeMillis;
        }

        @Override
        public boolean tryAcquire() {
            long now = System.currentTimeMillis();
            long boundary = now - windowSizeMillis;
            lock.lock();
            try {
                // Remove timestamps older than the sliding window boundary
                while (!requestTimestamps.isEmpty() && requestTimestamps.peekFirst() <= boundary) {
                    requestTimestamps.pollFirst();
                }

                if (requestTimestamps.size() < limit) {
                    requestTimestamps.addLast(now);
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }
    }

    // --- 6. Main Method Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== 1. Demonstrating Token Bucket Policy ===");
        RateLimiter tokenBucket = new TokenBucketRateLimiter(3, 2); // Cap: 3, Refill: 2/sec
        for (int i = 1; i <= 5; i++) {
            boolean allowed = tokenBucket.tryAcquire();
            System.out.println("Token Bucket Request " + i + " -> " + (allowed ? "ALLOWED" : "THROTTLED"));
            Thread.sleep(100);
        }

        System.out.println("\nWaiting 1.5 seconds for token replenishment...");
        Thread.sleep(1500);

        for (int i = 6; i <= 8; i++) {
            boolean allowed = tokenBucket.tryAcquire();
            System.out.println("Token Bucket Request " + i + " -> " + (allowed ? "ALLOWED" : "THROTTLED"));
        }

        System.out.println("\n=== 2. Demonstrating Leaky Bucket Policy ===");
        RateLimiter leakyBucket = new LeakyBucketRateLimiter(3, 2); // Cap: 3, Leak: 2/sec
        for (int i = 1; i <= 5; i++) {
            boolean allowed = leakyBucket.tryAcquire();
            System.out.println("Leaky Bucket Request " + i + " -> " + (allowed ? "ALLOWED" : "THROTTLED"));
            Thread.sleep(100);
        }

        System.out.println("\nWaiting 1.5 seconds for token replenishment...");
        Thread.sleep(1500);

        for (int i = 6; i <= 8; i++) {
            boolean allowed = leakyBucket.tryAcquire();
            System.out.println("Leaky Bucket Request " + i + " -> " + (allowed ? "ALLOWED" : "THROTTLED"));
        }

        System.out.println("\n=== 3. Demonstrating Fixed Window Counter Policy ===");
        RateLimiter fixedWindow = new FixedWindowRateLimiter(3, 1000); // 3 reqs per 1000ms
        for (int i = 1; i <= 5; i++) {
            boolean allowed = fixedWindow.tryAcquire();
            System.out.println("Fixed Window Request " + i + " -> " + (allowed ? "ALLOWED" : "THROTTLED"));
        }

        System.out.println("\nWaiting for the next fixed window block (1.5 seconds)...");
        Thread.sleep(1500);
        for (int i = 6; i <= 8; i++) {
            boolean allowed = fixedWindow.tryAcquire();
            System.out.println("Fixed Window Request " + i + " -> " + (allowed ? "ALLOWED" : "THROTTLED"));
        }

        System.out.println("\n=== 4. Demonstrating Sliding Window Log Policy ===");
        RateLimiter slidingWindow = new SlidingWindowRateLimiter(3, 1000); // 3 reqs per rolling 1000ms
        for (int i = 1; i <= 5; i++) {
            boolean allowed = slidingWindow.tryAcquire();
            System.out.println("Sliding Window Request " + i + " -> " + (allowed ? "ALLOWED" : "THROTTLED"));
            Thread.sleep(50);
        }

        System.out.println("\nWaiting for the sliding window to allow more requests (1.5 seconds)...");
        Thread.sleep(1500);
        for (int i = 6; i <= 8; i++) {
            boolean allowed = slidingWindow.tryAcquire();
            System.out.println("Sliding Window Request " + i + " -> " + (allowed ? "ALLOWED" : "THROTTLED"));
        }
    }
}
