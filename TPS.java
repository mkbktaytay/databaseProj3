import java.sql.*;
import java.util.concurrent.*;

public class TPS {

    // PostgreSQL Connection Parameters
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68";

    // openGauss Connection Parameters
    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@";

    // Number of threads for concurrent transactions
    private static final int THREADS = 1;  // Set threads to 1

    // Number of transactions per thread
    private static final int TRANSACTIONS_PER_THREAD = 5;

    // SQL queries for workloads
    private static final String[] SQL_QUERIES = {
            "INSERT INTO customers (name, age, city) VALUES ('Alice', 30, 'Los Angeles');", // Insert
            "UPDATE customers SET city = 'San Francisco' WHERE name = 'Alice';",           // Update
            "SELECT id, name, city FROM customers;",                                       // Select
            "DELETE FROM customers WHERE name = 'Alice';"                                 // Delete
    };

    // Workload names
    private static final String[] WORKLOAD_TYPES = {
            "Insert Workload",
            "Update Workload",
            "Select Workload",
            "Delete Workload"
    };

    public static void main(String[] args) {
        try {
            for (int i = 0; i < WORKLOAD_TYPES.length; i++) {
                String workloadType = WORKLOAD_TYPES[i];
                String query = SQL_QUERIES[i];

                System.out.printf("Workload: %s\n", workloadType);
                System.out.printf("PostgreSQL:\n");
                double pgTps = testDatabasePerformance(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, query);
                System.out.printf("TPS: %.2f | Threads: %d\n", pgTps, THREADS);

                System.out.printf("openGauss:\n");
                double ogTps = testDatabasePerformance(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, query);
                System.out.printf("TPS: %.2f | Threads: %d\n", ogTps, THREADS);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static double testDatabasePerformance(String url, String user, String password, String query) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        long startTime = System.currentTimeMillis();

        // Only one thread, so submit just one task
        for (int i = 0; i < THREADS; i++) {
            executor.submit(new TransactionTask(url, user, password, query, TRANSACTIONS_PER_THREAD));
        }

        executor.shutdown();
        // Increase timeout to 120 seconds to allow more time for completion
        if (!executor.awaitTermination(120, TimeUnit.SECONDS)) {
            // If not terminated within the timeout, force shutdown
            executor.shutdownNow();
            throw new RuntimeException("Executor did not terminate in time.");
        }

        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;
        return (THREADS * TRANSACTIONS_PER_THREAD) / totalTimeInSeconds;
    }

    static class TransactionTask implements Runnable {
        private final String url;
        private final String user;
        private final String password;
        private final String query;
        private final int transactions;

        public TransactionTask(String url, String user, String password, String query, int transactions) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.query = query;
            this.transactions = transactions;
        }

        @Override
        public void run() {
            int retries = 3;  // Set retry count in case of deadlock

            while (retries > 0) {
                try (Connection connection = DriverManager.getConnection(url, user, password)) {
                    connection.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); // Set isolation level
                    connection.setAutoCommit(true);

                    try (Statement statement = connection.createStatement()) {
                        long transactionStartTime = System.currentTimeMillis();
                        for (int i = 0; i < transactions; i++) {
                                statement.executeUpdate(query);
                        }
                        long transactionEndTime = System.currentTimeMillis();
                        System.out.printf("Thread %d completed %d transactions in %d ms\n",
                                Thread.currentThread().getId(), transactions, (transactionEndTime - transactionStartTime));
                        break;  // If successful, break out of the retry loop
                    }
                } catch (SQLException e) {
                    if (e.getMessage().contains("deadlock detected") && retries > 0) {
                        System.out.println("Deadlock detected, retrying...");
                        retries--;
                        try {
                            Thread.sleep(1000); // Sleep for 1 second before retrying
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        }
                    } else {
                        System.err.println("Error executing query: " + query);
                        e.printStackTrace();
                        break;  // If not a deadlock, break the loop
                    }
                }
            }
        }
    }
}
