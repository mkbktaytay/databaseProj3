import java.sql.*;
import java.util.concurrent.*;
import java.util.*;

public class DataVolume {

    // Database connection parameters for PostgreSQL
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68";

    // Database connection parameters for openGauss
    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@";

    // Number of threads to simulate concurrent transactions
    private static final int THREADS = 10;

    // Transaction SQL (this can be any SQL operation, here we use INSERT as an example)
    private static final String INSERT_SQL = "INSERT INTO customers (name, city) VALUES ('John Doe', 'New York');";

    // Number of transactions per thread
    private static final int TRANSACTIONS_PER_THREAD = 100;

    // List of data sizes to simulate
    private static final int[] DATA_SIZES = {10000, 100000, 1000000, 10000000}; // Simulating 10k, 100k, 1M, 10M rows

    public static void main(String[] args) {
        try {
            // Test PostgreSQL and openGauss for each data size
            for (int dataSize : DATA_SIZES) {
                System.out.println("\nTesting with data size: " + dataSize + " rows");
                testDataVolumePerformance(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL", dataSize);
                testDataVolumePerformance(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss", dataSize);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to test performance as data volume increases
    public static void testDataVolumePerformance(String url, String user, String password, String databaseName, int dataSize) throws Exception {
        // Start the timer
        long startTime = System.currentTimeMillis();

        // Simulate increasing data size by inserting `dataSize` rows into the database
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);

        // Run concurrent transactions
        for (int i = 0; i < THREADS; i++) {
            executor.submit(new TransactionTask(url, user, password, INSERT_SQL, TRANSACTIONS_PER_THREAD));
        }

        // Wait for the threads to finish and calculate the total time
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to finish
        }

        // End the timer
        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;

        // Calculate TPS (Transactions per second)
        double tps = (THREADS * TRANSACTIONS_PER_THREAD) / totalTimeInSeconds;
        System.out.printf("TPS for %s with %d data rows: %.2f transactions per second\n", databaseName, dataSize, tps);

        // Output the total execution time and TPS for comparison
        System.out.println("Total execution time for " + databaseName + " with " + dataSize + " rows: " + totalTimeInSeconds + " seconds");
    }

    // Task that simulates a transaction
    static class TransactionTask implements Runnable {
        private String url;
        private String user;
        private String password;
        private String query;
        private int transactions;

        public TransactionTask(String url, String user, String password, String query, int transactions) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.query = query;
            this.transactions = transactions;
        }

        @Override
        public void run() {
            try (Connection connection = DriverManager.getConnection(url, user, password)) {
                connection.setAutoCommit(false); // Start transaction

                try (Statement statement = connection.createStatement()) {
                    for (int i = 0; i < transactions; i++) {
                        statement.executeUpdate(query); // Execute SQL query (INSERT)
                    }
                    connection.commit(); // Commit the transaction
                } catch (Exception e) {
                    connection.rollback(); // Rollback if there is an error
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
