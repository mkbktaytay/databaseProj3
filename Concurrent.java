import java.sql.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Concurrent {

    // Database connection parameters for PostgreSQL
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68"; // Replace with actual password

    // Database connection parameters for openGauss
    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@"; // Replace with actual password

    // Number of concurrent users to test
    private static final int[] CONCURRENT_USERS = {10, 20, 50, 100};

    // SQL Query to execute (SELECT for testing)
    private static final String TEST_QUERY = "SELECT * FROM customers LIMIT 1;"; // Simple SELECT query for testing

    // Duration for the test (in seconds)
    private static final int TEST_DURATION = 60; // 1 minute for each test

    // Logger for better error handling
    private static final Logger logger = Logger.getLogger(Concurrent.class.getName());

    public static void main(String[] args) {
        try {


            // Test openGauss for different concurrent users
            for (int users : CONCURRENT_USERS) {
                System.out.println("\nTesting with " + users + " concurrent users on openGauss:");
                testDatabasePerformance(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss", users);
            }

            // Test PostgreSQL for different concurrent users
            for (int users : CONCURRENT_USERS) {
                System.out.println("\nTesting with " + users + " concurrent users on PostgreSQL:");
                testDatabasePerformance(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL", users);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to test database performance
    public static void testDatabasePerformance(String url, String user, String password, String databaseName, int concurrentUsers) throws Exception {
        long startTime = System.currentTimeMillis();

        // Executor service to simulate concurrent users with limited threads
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);

        // Submit tasks to simulate concurrent users (threads)
        for (int i = 0; i < concurrentUsers; i++) {
            executor.submit(new UserRequestTask(url, user, password, TEST_QUERY));
        }

        // Wait for all tasks to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait until all tasks are finished
        }

        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;

        // Calculate TPS (Transactions per second)
        double tps = (concurrentUsers * TEST_DURATION) / totalTimeInSeconds;

        // Print the results directly
        System.out.printf("%s with %d concurrent users: TPS = %.2f, Total Time = %.2f seconds\n", databaseName, concurrentUsers, tps, totalTimeInSeconds);
    }

    // Task to simulate a user making a request
    static class UserRequestTask implements Runnable {
        private String url;
        private String user;
        private String password;
        private String query;

        public UserRequestTask(String url, String user, String password, String query) {
            this.url = url;
            this.user = user;
            this.password = password;
            this.query = query;
        }

        @Override
        public void run() {
            int retryCount = 3;  // Retry up to 3 times
            while (retryCount > 0) {
                try (Connection connection = DriverManager.getConnection(url, user, password)) {
                    // Execute the query (simulate a user request)
                    try (Statement statement = connection.createStatement()) {
                        statement.executeQuery(query);  // Execute the query (SELECT)
                    }
                    break;  // Exit the loop if successful
                } catch (SQLException e) {
                    retryCount--;
                    logger.log(Level.WARNING, "SQL exception occurred, retrying... Attempts left: " + retryCount, e);
                    try {
                        Thread.sleep(500);  // Adding delay before retry
                    } catch (InterruptedException interruptedException) {
                        Thread.currentThread().interrupt();  // Handle thread interruption
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unexpected exception", e);
                    break;
                }
            }
        }
    }
}
