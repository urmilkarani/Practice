package org.example.lowlevel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SimpleConnectionPool {

    // --- Mock Connection Class ---
    public static class Connection {
        private final int id;

        public Connection(int id) {
            this.id = id;
            // Simulate expensive connection initialization (e.g., TCP handshake, authentication)
            System.out.println("-> Initialized physical database Connection " + id);
        }

        public void executeQuery(String query) {
            System.out.println("[Connection " + id + "] Executing query: " + query);
        }

        public int getId() {
            return id;
        }

        public void close() {
            System.out.println("<- Closed physical connection " + id);
        }
    }

    private final int poolSize;
    private final BlockingQueue<Connection> availableConnections;
    private final List<Connection> allConnections;

    public SimpleConnectionPool(int poolSize) {
        if (poolSize <= 0) {
            throw new IllegalArgumentException("Pool size must be greater than 0");
        }
        this.poolSize = poolSize;
        this.availableConnections = new LinkedBlockingQueue<>(poolSize);
        this.allConnections = new ArrayList<>(poolSize);

        // Pre-allocate connections upfront (Eager Initialization)
        for (int i = 1; i <= poolSize; i++) {
            Connection conn = new Connection(i);
            allConnections.add(conn);
            availableConnections.offer(conn);
        }
    }

    // --- Acquire Connection ---
    public Connection getConnection() throws InterruptedException {
        // Blocks automatically if no connections are currently available
        return availableConnections.take();
    }

    // --- Release Connection ---
    public void releaseConnection(Connection connection) {
        if (connection == null) {
            return;
        }
        // Return the connection back to the pool for reuse
        availableConnections.offer(connection);
    }

    // --- Graceful Shutdown ---
    public void shutdown() {
        System.out.println("\nInitiating connection pool shutdown...");
        availableConnections.clear();
        for (Connection conn : allConnections) {
            conn.close();
        }
    }

    // --- Main Method for Demonstration ---
    public static void main(String[] args) throws InterruptedException {
        int poolSize = 3;
        SimpleConnectionPool pool = new SimpleConnectionPool(poolSize);

        System.out.println("\nPool initialized with " + poolSize + " connections. Starting client threads...\n");

        // Simulate 6 concurrent clients requesting connections from a pool of size 3
        int numberOfClients = 6;
        Thread[] clients = new Thread[numberOfClients];

        for (int i = 1; i <= numberOfClients; i++) {
            final int clientId = i;
            clients[i - 1] = new Thread(() -> {
                try {
                    System.out.println("Client " + clientId + " requesting a connection...");
                    Connection conn = pool.getConnection();
                    System.out.println("==> Client " + clientId + " acquired Connection " + conn.getId());

                    // Simulate database work
                    Thread.sleep(800);
                    conn.executeQuery("SELECT * FROM users WHERE client_id = " + clientId);

                    System.out.println("<== Client " + clientId + " releasing Connection " + conn.getId());
                    pool.releaseConnection(conn);

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.err.println("Client " + clientId + " was interrupted.");
                }
            }, "Client-" + i);
            clients[i - 1].start();
        }

        // Wait for all client threads to finish work
        for (Thread client : clients) {
            client.join();
        }

        // Shutdown the pool and clean up resources
        pool.shutdown();
    }
}
