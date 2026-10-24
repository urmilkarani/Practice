package org.example.lowlevel;

import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LargeFileTransferService {

    // --- 1. Transfer Listener Interface ---
    public interface TransferListener {
        void onProgress(String transferId, long bytesTransferred, long totalBytes, int percent);
        void onComplete(String transferId, File file);
        void onError(String transferId, Throwable throwable);
    }

    // --- 2. Chunk Metadata Definition ---
    public static class ChunkMeta {
        private final int chunkIndex;
        private final long offset;
        private final int length;
        private final String checksum;

        public ChunkMeta(int chunkIndex, long offset, int length, String checksum) {
            this.chunkIndex = chunkIndex;
            this.offset = offset;
            this.length = length;
            this.checksum = checksum;
        }

        public int getChunkIndex() { return chunkIndex; }
        public long getOffset() { return offset; }
        public int getLength() { return length; }
        public String getChecksum() { return checksum; }
    }

    // Helper method to compute MD5 checksum for integrity checks
    public static String calculateChecksum(byte[] data, int offset, int length) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(data, offset, length);
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }

    // --- 3. Transfer Manager Core ---
    private final ExecutorService executorService;
    private final int chunkSize;

    public LargeFileTransferService(int maxThreads, int chunkSize) {
        this.executorService = Executors.newFixedThreadPool(maxThreads);
        this.chunkSize = chunkSize;
    }

    // Uploads a file by splitting it into concurrent chunk transfer tasks
    public void uploadFile(String transferId, File sourceFile, TransferListener listener) {
        executorService.submit(() -> {
            try {
                if (!sourceFile.exists()) {
                    throw new FileNotFoundException("Source file not found: " + sourceFile.getAbsolutePath());
                }

                long totalBytes = sourceFile.length();
                List<ChunkMeta> chunks = new ArrayList<>();

                // 1. Scan and split file into chunk metadata segments
                try (RandomAccessFile raf = new RandomAccessFile(sourceFile, "r")) {
                    long offset = 0;
                    int chunkIndex = 0;
                    byte[] readBuffer = new byte[chunkSize];
                    while (offset < totalBytes) {
                        int bytesToRead = (int) Math.min(chunkSize, totalBytes - offset);
                        raf.seek(offset);
                        raf.readFully(readBuffer, 0, bytesToRead);
                        String checksum = calculateChecksum(readBuffer, 0, bytesToRead);
                        chunks.add(new ChunkMeta(chunkIndex, offset, bytesToRead, checksum));
                        offset += bytesToRead;
                        chunkIndex++;
                    }
                }

                System.out.printf("File [%s] split into %d chunks. Starting parallel upload...\n", transferId, chunks.size());

                // 2. Dispatch chunks to executor via CompletionService for asynchronous aggregation
                AtomicLong transferredBytes = new AtomicLong(0);
                CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executorService);

                for (ChunkMeta chunk : chunks) {
                    completionService.submit(() -> uploadChunkWithRetry(sourceFile, chunk, transferId, transferredBytes, totalBytes, listener));
                }

                // 3. Monitor chunk completion results
                for (int i = 0; i < chunks.size(); i++) {
                    Future<Boolean> future = completionService.take();
                    if (!future.get()) {
                        throw new IOException("Aborting transfer [" + transferId + "] due to chunk upload failure.");
                    }
                }

                if (listener != null) {
                    listener.onComplete(transferId, sourceFile);
                }

            } catch (Throwable t) {
                if (listener != null) {
                    listener.onError(transferId, t);
                }
            }
        });
    }

    // Uploads an individual chunk with built-in retry logic and checksum validation
    private boolean uploadChunkWithRetry(File file, ChunkMeta chunk, String transferId, AtomicLong transferredBytes, long totalBytes, TransferListener listener) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
                byte[] buffer = new byte[chunk.getLength()];
                raf.seek(chunk.getOffset());
                raf.readFully(buffer);

                // Verify integrity via checksum before sending over the wire
                String verifiedChecksum = calculateChecksum(buffer, 0, buffer.length);
                if (!verifiedChecksum.equals(chunk.getChecksum())) {
                    throw new SecurityException("Checksum mismatch on chunk index: " + chunk.getChunkIndex());
                }

                // Simulate network transmission latency
                Thread.sleep(100);

                // Update cumulative transferred bytes and fire progress callback
                long currentTransferred = transferredBytes.addAndGet(chunk.getLength());
                int percent = (totalBytes > 0) ? (int) ((currentTransferred * 100) / totalBytes) : 100;

                if (listener != null) {
                    listener.onProgress(transferId, currentTransferred, totalBytes, percent);
                }

                return true;
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxRetries) {
                    System.err.println("Chunk " + chunk.getChunkIndex() + " failed permanently after " + maxRetries + " attempts: " + e.getMessage());
                    return false;
                }
                try {
                    Thread.sleep(200L * attempt); // Exponential backoff simulation
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
        }
        return false;
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    // --- 4. Main Method Demonstration ---
    public static void main(String[] args) throws Exception {
        // Initialize service with 4 worker threads and 1MB chunk size
        LargeFileTransferService transferService = new LargeFileTransferService(4, 1024 * 1024);

        // Create a temporary large sample file (~3.5 MB)
        File demoFile = new File(System.getProperty("java.io.tmpdir"), "large_sample_file.dat");
        try (FileOutputStream fos = new FileOutputStream(demoFile)) {
            byte[] block = new byte[1024 * 512]; // 512 KB blocks
            new Random().nextBytes(block);
            for (int i = 0; i < 7; i++) {
                fos.write(block);
            }
        }

        System.out.println("Created demo file: " + demoFile.getAbsolutePath() + " (" + demoFile.length() + " bytes)");

        TransferListener listener = new TransferListener() {
            @Override
            public void onProgress(String transferId, long bytesTransferred, long totalBytes, int percent) {
                System.out.printf("[%s] Progress: %d%% (%d / %d bytes)\n", transferId, percent, bytesTransferred, totalBytes);
            }

            @Override
            public void onComplete(String transferId, File file) {
                System.out.println("✅ Transfer completed successfully for: " + transferId);
            }

            @Override
            public void onError(String transferId, Throwable throwable) {
                System.err.println("❌ Transfer failed for [" + transferId + "]: " + throwable.getMessage());
            }
        };

        System.out.println("\nInitiating large file upload transfer...");
        transferService.uploadFile("transfer-uuid-999", demoFile, listener);

        // Wait for asynchronous transfer to complete
        Thread.sleep(4000);

        System.out.println("\nShutting down transfer service...");
        transferService.shutdown();
        System.out.println("Transfer service shutdown complete.");

        // Clean up temporary demo file
        if (demoFile.exists()) {
            demoFile.delete();
        }
    }
}
