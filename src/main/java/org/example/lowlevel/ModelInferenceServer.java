package org.example.lowlevel;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.List;
import java.util.ArrayList;

public class ModelInferenceServer {

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

    // --- Model Interface ---
    public interface Model {
        String predict(String input) throws Exception;
    }

    // --- Mock Heavy ML Model ---
    public static class HeavyMLModel implements Model {
        @Override
        public String predict(String input) throws Exception {
            // Simulate heavy computation / GPU inference latency
            Thread.sleep(60);
            return "Prediction[" + input.toUpperCase() + "]";
        }
    }

    // --- Server Core Components ---
    private final BlockingQueue<InferenceRequest> requestQueue;
    private final ExecutorService workerPool;
    private final AtomicBoolean isShutdown = new AtomicBoolean(false);
    private final Model model;
    private final List<Thread> workers;

    public ModelInferenceServer(Model model, int workerThreads, int queueCapacity) {
        this.model = model;
        this.requestQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.workerPool = Executors.newFixedThreadPool(workerThreads);
        this.workers = new ArrayList<>();

        // Start worker threads to process incoming inference requests
        for (int i = 0; i < workerThreads; i++) {
            Thread worker = new Thread(this::processQueue, "Inference-Worker-" + i);
            worker.setDaemon(true);
            workers.add(worker);
            worker.start();
        }
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
            // Queue is full: apply backpressure and reject request gracefully
            CompletableFuture<InferenceResponse> failedFuture = new CompletableFuture<>();
            failedFuture.completeExceptionally(new RejectedExecutionException("Inference queue is full. Backpressure applied."));
            return failedFuture;
        }

        return request.getFuture();
    }

    // --- Worker Queue Loop ---
    private void processQueue() {
        while (!isShutdown.get() || !requestQueue.isEmpty()) {
            try {
                InferenceRequest request = requestQueue.poll(100, TimeUnit.MILLISECONDS);
                if (request == null) {
                    continue;
                }

                long startTime = System.currentTimeMillis();
                try {
                    String prediction = model.predict(request.getInputData());
                    long latency = System.currentTimeMillis() - startTime;
                    request.getFuture().complete(
                            new InferenceResponse(request.getRequestId(), prediction, latency, true, null)
                    );
                } catch (Exception e) {
                    long latency = System.currentTimeMillis() - startTime;
                    request.getFuture().complete(
                            new InferenceResponse(request.getRequestId(), null, latency, false, e.getMessage())
                    );
                }

            } catch (InterruptedException e) {
                if (isShutdown.get()) {
                    break;
                }
                Thread.currentThread().interrupt();
            }
        }
    }

    // --- Graceful Shutdown ---
    public void shutdown() {
        isShutdown.set(true);
        for (Thread worker : workers) {
            worker.interrupt();
        }
        workerPool.shutdown();
        try {
            if (!workerPool.awaitTermination(3, TimeUnit.SECONDS)) {
                workerPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            workerPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
        System.out.println("Model Inference Server shut down successfully.");
    }

    // --- Main Method Demonstration ---
    public static void main(String[] args) throws Exception {
        Model model = new HeavyMLModel();
        // Initialize server with 3 concurrent worker threads and a request queue capacity of 10
        ModelInferenceServer server = new ModelInferenceServer(model, 3, 10);

        System.out.println("Submitting concurrent inference requests...");

        List<CompletableFuture<InferenceResponse>> futures = new ArrayList<>();

        // Submit 8 concurrent inference requests
        for (int i = 1; i <= 8; i++) {
            final String reqId = "REQ-" + i;
            final String input = "payload_" + i;
            CompletableFuture<InferenceResponse> future = server.infer(reqId, input);
            futures.add(future);
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
