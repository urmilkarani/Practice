package org.example.lowlevel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class FileDownloadManager {

    // --- 1. Progress Listener Interface ---
    public interface DownloadListener {
        void onProgress(String fileId, long bytesRead, long totalBytes, int percent);
        void onComplete(String fileId, File destination);
        void onError(String fileId, Throwable throwable);
    }

    // --- 2. Download Task Definition ---
    public static class DownloadTask implements Runnable {
        private final String fileId;
        private final String fileUrl;
        private final File destinationFile;
        private final DownloadListener listener;
        private final AtomicBoolean isCancelled = new AtomicBoolean(false);

        public DownloadTask(String fileId, String fileUrl, File destinationFile, DownloadListener listener) {
            this.fileId = fileId;
            this.fileUrl = fileUrl;
            this.destinationFile = destinationFile;
            this.listener = listener;
        }

        public void cancel() {
            isCancelled.set(true);
        }

        @Override
        public void run() {
            HttpURLConnection connection = null;
            try {
                URL url = URI.create(fileUrl).toURL();
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();

                int responseCode = connection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw new IOException("Server returned HTTP response code: " + responseCode);
                }

                long totalBytes = connection.getContentLengthLong();
                long bytesRead = 0;
                int lastPercent = -1;

                try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream fos = new FileOutputStream(destinationFile)) {

                    byte[] buffer = new byte[8192];
                    int bytesReadChunk;

                    while ((bytesReadChunk = in.read(buffer)) != -1) {
                        if (isCancelled.get()) {
                            System.out.println("Download cancelled for: " + fileId);
                            return;
                        }

                        fos.write(buffer, 0, bytesReadChunk);
                        bytesRead += bytesReadChunk;

                        int percent = (totalBytes > 0) ? (int) ((bytesRead * 100) / totalBytes) : -1;

                        // Trigger progress update only when percentage changes to reduce overhead
                        if (percent != lastPercent) {
                            lastPercent = percent;
                            if (listener != null) {
                                listener.onProgress(fileId, bytesRead, totalBytes, percent);
                            }
                        }
                    }
                }

                if (!isCancelled.get() && listener != null) {
                    listener.onComplete(fileId, destinationFile);
                }

            } catch (Exception e) {
                if (!isCancelled.get() && listener != null) {
                    listener.onError(fileId, e);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }

    // --- 3. Download Manager Core ---
    private final ExecutorService executorService;
    private final Map<String, DownloadTask> activeDownloads;

    public FileDownloadManager(int maxConcurrentDownloads) {
        this.executorService = Executors.newFixedThreadPool(maxConcurrentDownloads);
        this.activeDownloads = new ConcurrentHashMap<>();
    }

    public void submitDownload(String fileId, String fileUrl, File destinationFile, DownloadListener listener) {
        if (activeDownloads.containsKey(fileId)) {
            throw new IllegalArgumentException("Download with ID " + fileId + " is already active.");
        }

        DownloadTask task = new DownloadTask(fileId, fileUrl, destinationFile, listener);
        activeDownloads.put(fileId, task);

        // Wrap execution to clean up active map upon completion
        executorService.submit(() -> {
            try {
                task.run();
            } finally {
                activeDownloads.remove(fileId);
            }
        });
    }

    public void cancelDownload(String fileId) {
        DownloadTask task = activeDownloads.get(fileId);
        if (task != null) {
            task.cancel();
        }
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
    public static void main(String[] args) throws InterruptedException {
        FileDownloadManager downloadManager = new FileDownloadManager(2);

        // Create temporary directory for downloads
        File downloadDir = new File(System.getProperty("java.io.tmpdir"), "download_manager_demo");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        DownloadListener listener = new DownloadListener() {
            @Override
            public void onProgress(String fileId, long bytesRead, long totalBytes, int percent) {
                System.out.printf("[%s] Progress: %d%% (%d / %d bytes)\n", fileId, percent, bytesRead, totalBytes);
            }

            @Override
            public void onComplete(String fileId, File destination) {
                System.out.println("✅ Successfully downloaded [" + fileId + "] to " + destination.getAbsolutePath());
            }

            @Override
            public void onError(String fileId, Throwable throwable) {
                System.err.println("❌ Error downloading [" + fileId + "]: " + throwable.getMessage());
            }
        };

        // Using reliable public sample URLs for demonstration
        File file1 = new File(downloadDir, "sample_1mb.bin");
        File file2 = new File(downloadDir, "sample_json.json");

        System.out.println("Submitting download tasks...");
        downloadManager.submitDownload(
                "file-1",
                "https://raw.githubusercontent.com/ozh/github-logos/master/png/github-mark.png",
                file1,
                listener
        );

        downloadManager.submitDownload(
                "file-2",
                "https://httpbin.org/json",
                file2,
                listener
        );

        // Allow time for downloads to finish
        Thread.sleep(5000);

        System.out.println("\nShutting down download manager...");
        downloadManager.shutdown();
        System.out.println("Download manager shut down cleanly.");
    }
}
