package org.example.lowlevel;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

public class MetricsCollectorDemo {

    // --- 1. Metric Types ---

    /**
     * Counter: Used for tracking discrete increments (e.g., requests, errors, events).
     * Utilizes LongAdder for low contention under heavy concurrent writes.
     */
    public static class Counter {
        private final LongAdder adder = new LongAdder();

        public void increment() {
            adder.increment();
        }

        public void add(long delta) {
            adder.add(delta);
        }

        public long get() {
            return adder.sum();
        }
    }

    /**
     * Gauge: Represents a point-in-time value via a dynamic supplier (e.g., memory usage, queue size).
     */
    public static class Gauge {
        private final Supplier<Number> supplier;

        public Gauge(Supplier<Number> supplier) {
            this.supplier = supplier;
        }

        public double getValue() {
            return supplier.get().doubleValue();
        }
    }

    /**
     * TimerSummary: Tracks latency/duration metrics including count, total time, min, max, and average.
     */
    public static class TimerSummary {
        private final LongAdder count = new LongAdder();
        private final LongAdder totalTimeMillis = new LongAdder();
        private long min = Long.MAX_VALUE;
        private long max = Long.MIN_VALUE;
        private final Object lock = new Object();

        public void record(long durationMillis) {
            count.increment();
            totalTimeMillis.add(durationMillis);
            synchronized (lock) {
                if (durationMillis < min) min = durationMillis;
                if (durationMillis > max) max = durationMillis;
            }
        }

        public TimerSnapshot getSnapshot() {
            long c = count.sum();
            long total = totalTimeMillis.sum();
            double avg = c == 0 ? 0.0 : (double) total / c;
            long currentMin, currentMax;
            synchronized (lock) {
                currentMin = c == 0 ? 0 : min;
                currentMax = c == 0 ? 0 : max;
            }
            return new TimerSnapshot(c, total, avg, currentMin, currentMax);
        }
    }

    public static class TimerSnapshot {
        public final long count;
        public final long totalMillis;
        public final double averageMillis;
        public final long minMillis;
        public final long maxMillis;

        public TimerSnapshot(long count, long totalMillis, double averageMillis, long minMillis, long maxMillis) {
            this.count = count;
            this.totalMillis = totalMillis;
            this.averageMillis = averageMillis;
            this.minMillis = minMillis;
            this.maxMillis = maxMillis;
        }

        @Override
        public String toString() {
            return String.format("count=%d, totalMs=%d, avgMs=%.2f, minMs=%d, maxMs=%d",
                    count, totalMillis, averageMillis, minMillis, maxMillis);
        }
    }

    // --- 2. Central Metrics Registry ---

    public static class MetricsRegistry {
        private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, Gauge> gauges = new ConcurrentHashMap<>();
        private final ConcurrentHashMap<String, TimerSummary> timers = new ConcurrentHashMap<>();

        public Counter counter(String name) {
            return counters.computeIfAbsent(name, k -> new Counter());
        }

        public void registerGauge(String name, Supplier<Number> supplier) {
            gauges.put(name, new Gauge(supplier));
        }

        public TimerSummary timer(String name) {
            return timers.computeIfAbsent(name, k -> new TimerSummary());
        }

        public void report() {
            System.out.println("\n=== METRICS REPORT SNAPSHOT ===");

            System.out.println("Counters:");
            counters.forEach((name, counter) ->
                    System.out.println("  - " + name + " = " + counter.get())
            );

            System.out.println("Gauges:");
            gauges.forEach((name, gauge) ->
                    System.out.println("  - " + name + " = " + gauge.getValue())
            );

            System.out.println("Timers:");
            timers.forEach((name, timer) ->
                    System.out.println("  - " + name + " -> " + timer.getSnapshot())
            );
            System.out.println("================================\n");
        }
    }

    // --- 3. Main Method Demonstration ---

    public static void main(String[] args) throws InterruptedException {
        MetricsRegistry registry = new MetricsRegistry();

        // Register system gauges
        registry.registerGauge("jvm.memory.free_mb", () -> Runtime.getRuntime().freeMemory() / (1024 * 1024));
        registry.registerGauge("system.active_threads", Thread::activeCount);

        // Simulate concurrent worker threads recording metrics
        ExecutorService executor = Executors.newFixedThreadPool(4);

        System.out.println("Starting concurrent metric recording tasks...");
        for (int i = 0; i < 4; i++) {
            executor.submit(() -> {
                Counter requestCounter = registry.counter("http.requests.total");
                Counter errorCounter = registry.counter("http.requests.errors");
                TimerSummary dbTimer = registry.timer("db.query.latency");

                for (int j = 0; j < 25; j++) {
                    requestCounter.increment();

                    long start = System.currentTimeMillis();
                    try {
                        // Simulate varying database or API call latencies
                        Thread.sleep(ThreadLocalRandom.current().nextInt(5, 25));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    long duration = System.currentTimeMillis() - start;
                    dbTimer.record(duration);

                    // Simulate occasional application errors
                    if (j % 5 == 0) {
                        errorCounter.increment();
                    }
                }
            });
        }

        // Allow background workers time to execute
        Thread.sleep(400);

        // Trigger an intermediate snapshot report
        registry.report();

        // Shutdown executor and await completion
        executor.shutdown();
        executor.awaitTermination(2, TimeUnit.SECONDS);

        System.out.println("Final summary report after completion:");
        registry.report();
    }
}
