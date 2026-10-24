package org.example.lowlevel;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class GpuModelEvictionService {

    // --- Model Definition ---
    public static class Model {
        private final String modelId;
        private final long vramRequiredBytes;

        public Model(String modelId, long vramRequiredBytes) {
            this.modelId = modelId;
            this.vramRequiredBytes = vramRequiredBytes;
        }

        public String getModelId() { return modelId; }
        public long getVramRequiredBytes() { return vramRequiredBytes; }
    }

    // --- GPU Node Managing VRAM and LRU Eviction ---
    public static class GpuNode {
        private final int gpuId;
        private final long totalVramBytes;
        private long availableVramBytes;

        // LinkedHashMap with access-order (true) for LRU tracking
        private final Map<String, Model> loadedModels;
        private final ReentrantLock lock = new ReentrantLock();

        public GpuNode(int gpuId, long totalVramBytes) {
            this.gpuId = gpuId;
            this.totalVramBytes = totalVramBytes;
            this.availableVramBytes = totalVramBytes;
            this.loadedModels = new LinkedHashMap<>(16, 0.75f, true);
        }

        public int getGpuId() { return gpuId; }
        public long getAvailableVramBytes() { return availableVramBytes; }

        // Attempts to load a model, evicting older models if necessary
        public boolean loadModel(Model model) {
            lock.lock();
            try {
                // If model is already loaded, just touch it (LRU update)
                if (loadedModels.containsKey(model.getModelId())) {
                    loadedModels.get(model.getModelId()); // Access updates order
                    System.out.println("[GPU-" + gpuId + "] Model [" + model.getModelId() + "] already loaded. Cache hit.");
                    return true;
                }

                // Check if model fits entirely in total GPU capacity
                if (model.getVramRequiredBytes() > totalVramBytes) {
                    System.err.println("[GPU-" + gpuId + "] Model [" + model.getModelId() +
                            "] requires " + model.getVramRequiredBytes() + " bytes, exceeding total GPU VRAM (" + totalVramBytes + ").");
                    return false;
                }

                // Evict LRU models until enough VRAM is free
                while (availableVramBytes < model.getVramRequiredBytes() && !loadedModels.isEmpty()) {
                    Iterator<Map.Entry<String, Model>> iterator = loadedModels.entrySet().iterator();
                    if (iterator.hasNext()) {
                        Map.Entry<String, Model> eldest = iterator.next();
                        Model evicted = eldest.getValue();
                        iterator.remove();
                        availableVramBytes += evicted.getVramRequiredBytes();
                        System.out.println("[GPU-" + gpuId + "] Evicted LRU Model [" + evicted.getModelId() +
                                "] freeing " + evicted.getVramRequiredBytes() + " bytes.");
                    }
                }

                // Final check if space is available after eviction
                if (availableVramBytes >= model.getVramRequiredBytes()) {
                    loadedModels.put(model.getModelId(), model);
                    availableVramBytes -= model.getVramRequiredBytes();
                    System.out.println("[GPU-" + gpuId + "] Successfully loaded Model [" + model.getModelId() +
                            "]. Remaining VRAM: " + availableVramBytes + "/" + totalVramBytes + " bytes.");
                    return true;
                }

                return false;
            } finally {
                lock.unlock();
            }
        }

        // Access model (refreshes LRU position)
        public boolean accessModel(String modelId) {
            lock.lock();
            try {
                if (loadedModels.containsKey(modelId)) {
                    // Accessing updates position in LinkedHashMap due to access-order = true
                    Model m = loadedModels.get(modelId);
                    return true;
                }
                return false;
            } finally {
                lock.unlock();
            }
        }

        // Explicit unloads / eviction
        public void evictModel(String modelId) {
            lock.lock();
            try {
                Model removed = loadedModels.remove(modelId);
                if (removed != null) {
                    availableVramBytes += removed.getVramRequiredBytes();
                    System.out.println("[GPU-" + gpuId + "] Explicitly unloaded Model [" + modelId +
                            "]. Free VRAM: " + availableVramBytes + "/" + totalVramBytes + " bytes.");
                }
            } finally {
                lock.unlock();
            }
        }

        public void printStatus() {
            lock.lock();
            try {
                System.out.println("  -> GPU [" + gpuId + "] Status: Free VRAM = " + availableVramBytes +
                        "/" + totalVramBytes + " bytes | Loaded Models: " + loadedModels.keySet());
            } finally {
                lock.unlock();
            }
        }
    }

    // --- Cluster Service Coordinator ---
    private final List<GpuNode> clusterGpus;

    public GpuModelEvictionService(int numGpus, long vramPerGpuBytes) {
        this.clusterGpus = new ArrayList<>();
        for (int i = 0; i < numGpus; i++) {
            clusterGpus.add(new GpuNode(i, vramPerGpuBytes));
        }
    }

    // Routes model load to the GPU with the most available VRAM (Best-Fit / Least Loaded)
    public boolean requestModelLoad(Model model) {
        GpuNode targetGpu = null;
        long maxFreeSpace = -1;

        // Find GPU with maximum available VRAM currently
        for (GpuNode gpu : clusterGpus) {
            if (gpu.getAvailableVramBytes() > maxFreeSpace) {
                maxFreeSpace = gpu.getAvailableVramBytes();
                targetGpu = gpu;
            }
        }

        if (targetGpu != null) {
            return targetGpu.loadModel(model);
        }
        return false;
    }

    public void printClusterStatus() {
        System.out.println("\n--- Cluster GPU Status ---");
        for (GpuNode gpu : clusterGpus) {
            gpu.printStatus();
        }
        System.out.println("--------------------------\n");
    }

    // --- Demonstration Main Method ---
    public static void main(String[] args) {
        // Initialize cluster with 2 GPUs, each having 1000 MB (represented in bytes/units) of VRAM
        GpuModelEvictionService service = new GpuModelEvictionService(2, 1000L);

        System.out.println("==================================================");
        System.out.println("   Initializing Model Loading & Eviction Service");
        System.out.println("==================================================\n");

        // Create models with different VRAM requirements
        Model llama7b = new Model("Llama-7B", 400L);
        Model mistral7b = new Model("Mistral-7B", 450L);
        Model bertBase = new Model("BERT-Base", 200L);
        Model gptJ = new Model("GPT-J-6B", 700L);

        // Load models into cluster
        service.requestModelLoad(llama7b);
        service.requestModelLoad(mistral7b);
        service.printClusterStatus();

        // Load a third model that forces an eviction on the most loaded GPU
        System.out.println(">> Requesting load for BERT-Base (should fit without eviction or trigger localized eviction)...");
        service.requestModelLoad(bertBase);
        service.printClusterStatus();

        // Load a massive model that forces LRU eviction due to VRAM limits
        System.out.println(">> Requesting load for GPT-J-6B (requires 700 bytes, forces eviction)...");
        service.requestModelLoad(gptJ);
        service.printClusterStatus();
    }
}
