package org.example.lowlevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DynamicBatchingInferenceServer {

    // --- Request & Response Wrappers ---
    public static class InferenceRequest {
        private final String requestId;
        private final String inputData;
        private final CompletableFuture<InferenceResponse> future;

        public InferenceRequest(String requestId, String inputData) {
            this.requestId = requestId;
            this.inputData = inputData;
            this.future = new CompletableFuture<>();
        }

        public String getRequestId() { return requestId; }
        public String getInputData() { return inputData; }
        public CompletableFuture<InferenceResponse> getFuture() { return future; }
    }

    public static class InferenceResponse {
        private final String requestId;
        private final String result;
        private final long latencyMs;
        private final boolean success;
        private final String errorMessage;

        public InferenceResponse(String requestId, String result, long latencyMs, boolean success, String errorMessage) {
            this.requestId = requestId;
            this.result = result;
            this.latencyMs = latencyMs;
            this.success = success;
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return String.format("InferenceResponse{id='%s', result='%s', latency=%dms, success=%s}",
                    requestId, result, latencyMs, success);
        }
    }

    // --- Batched Model Interface ---
    public interface BatchModel {
        List<String> predictBatch(List<String> inputs) throws Exception;
    }

    // --- Mock Heavy Batch ML Model ---
    public static class HeavyBatchMLModel implements BatchModel {
        @Override
        public List<String> predictBatch(List<String> inputs) throws Exception {
            // Simulate heavy GPU batch inference overhead (e.g., tensor parallel processing)
            Thread.sleep(40);
            List<String> results = new ArrayList<>();
            for (String input : inputs) {
                results.add("BatchPrediction[" + input.toUpperCase() + "]");
            }
            return results;
        }
    }

    // --- Server Core Components ---
    private final BlockingQueue<InferenceRequest> requestQueue;
    private final int maxBatchSize;
    private final long maxWaitTimeMs;
    private final BatchModel model;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final Thread batchingWorker;

    public DynamicBatchingInferenceServer(BatchModel model, int maxBatchSize, long maxWaitTimeMs, int queueCapacity) {
        this.model = model;
        this.maxBatchSize = maxBatchSize;
        this.maxWaitTimeMs = maxWaitTimeMs;
        this.requestQueue = new LinkedBlockingQueue<>(queueCapacity);

        // Start the background dynamic batching loop
        this.batchingWorker = new Thread(this::batchingLoop, "Dynamic-Batcher-Worker");
        this.batchingWorker.setDaemon(true);
        this.batchingWorker.start();
    }

    // --- Asynchronous Inference Submission ---
    public CompletableFuture<InferenceResponse> infer(String requestId, String inputData) {
        if (isShutdown.get()) {
            CompletableFuture<InferenceResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new IllegalStateException("Server is shut down."));
            return failedFuture;
        }

        InferenceRequest request = new InferenceRequest(requestId, inputData);
        boolean offered = requestQueue.offer(request);

        if (!offered) {
            CompletableFuture<InferenceResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RejectedExecutionException("Inference queue is full. Backpressure applied."));
            return failedFuture;
        }

        return request.getFuture();
    }

    // --- Dynamic Batching Scheduler Loop ---
    private void batchingLoop() {
        while (!isShutdown.get() || !requestQueue.isEmpty()) {
            try {
                // Block until the first request arrives
                InferenceRequest firstRequest = requestQueue.poll(100, TimeUnit.MILLISECONDS);
                if (firstRequest == null) {
                    continue;
                }

                List<InferenceRequest> currentBatch = new ArrayList<>();
                currentBatch.add(firstRequest);

                // Calculate timeout deadline for batch accumulation
                long deadline = System.currentTimeMillis() + maxWaitTimeMs;

                // Accumulate requests until maxBatchSize is reached or the timeout expires
                while (currentBatch.size() < maxBatchSize) {
                    long remainingTime = deadline - System.currentTimeMillis();
                    if (remainingTime <= 0) {
                        break;
                    }

                    InferenceRequest nextRequest = requestQueue.poll(remainingTime, TimeUnit.MILLISECONDS);
                    if (nextRequest == null) {
                        break; // Timeout expired before batch filled up
                    }
                    currentBatch.add(nextRequest);
                }

                // Execute batched inference
                processBatch(currentBatch);

            } catch (InterruptedException e) {
                if (isShutdown.get()) {
                    break;
                }
                Thread.currentThread().interrupt();
            }
        }
    }

    private void processBatch(List<InferenceRequest> batch) {
        long startTime = System.currentTimeMillis();
        try {
            List<String> inputs = new ArrayList<>();
            for (InferenceRequest req : batch) {
                inputs.add(req.getInputData());
            }

            // Perform single batched call to the underlying model
            List<String> outputs = model.predictBatch(inputs);
            long latency = System.currentTimeMillis() - startTime;

            // Distribute results back to individual futures
            for (int i = 0; i < batch.size(); i++) {
                InferenceRequest req = batch.get(i);
                String result = (i < outputs.size()) ? outputs.get(i) : "Error: Missing output";
                req.getFuture().complete(
                        new InferenceResponse(req.getRequestId(), result, latency, true, null)
                );
            }
        } catch (Exception e) {
            long latency = System.currentTimeMillis() - startTime;
            for (InferenceRequest req : batch) {
                req.getFuture().complete(
                        new InferenceResponse(req.getRequestId(), null, latency, false, e.getMessage())
                );
            }
        }
    }

    // --- Graceful Shutdown ---
    public void shutdown() {
        isShutdown.set(true);
        batchingWorker.interrupt();
        try {
            batchingWorker.join(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("Dynamic Batching Inference Server shut down successfully.");
    }

    // --- Main Method Demonstration ---
    public static void main(String[] args) throws Exception {
        BatchModel model = new HeavyBatchMLModel();

        // Configure server: Max batch size of 4, max wait timeout of 20ms, queue capacity of 20
        DynamicBatchingInferenceServer server = new DynamicBatchingInferenceServer(model, 4, 20, 20);

        System.out.println("Submitting concurrent inference requests to test dynamic batching...");

        List<CompletableFuture<InferenceResponse>> futures = new ArrayList<>();

        // Submit 6 requests almost simultaneously (will trigger a batch of 4 and a batch of 2)
        for (int i = 1; i <= 6; i++) {
            final String reqId = "REQ-" + i;
            final String input = "payload_" + i;
            futures.add(server.infer(reqId, input));
        }

        // Wait for all requests to finish asynchronously
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Print results
        for (CompletableFuture<InferenceResponse> future : futures) {
            System.out.println("Received -> " + future.get());
        }

        // Graceful shutdown
        server.shutdown();
    }
}
