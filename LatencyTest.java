import java.sql.*;
import java.util.concurrent.*;

public class LatencyTest {

    // Database connection parameters for PostgreSQL
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68";

    // Database connection parameters for openGauss
    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@";

    // Number of concurrent users (threads)
    private static final int MAX_CONCURRENT_USERS = 100;  // Adjust based on your requirements
    private static final int REQUESTS_PER_USER = 10; // Number of requests per user

    // SQL Query to execute (example SELECT query)
    private static final String SQL_QUERY = "SELECT name, city FROM customers;";

    public static void main(String[] args) {
        try {
            // Test PostgreSQL
            for (int concurrentUsers = 10; concurrentUsers <= MAX_CONCURRENT_USERS; concurrentUsers += 10) {
                System.out.printf("Testing PostgreSQL with %d concurrent users...\n", concurrentUsers);
                runLatencyTest(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, concurrentUsers);
            }

            // Test openGauss
            for (int concurrentUsers = 10; concurrentUsers <= MAX_CONCURRENT_USERS; concurrentUsers += 10) {
                System.out.printf("Testing openGauss with %d concurrent users...\n", concurrentUsers);
                runLatencyTest(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, concurrentUsers);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to run latency test with a specific database URL, user and password
    public static void runLatencyTest(String dbUrl, String dbUser, String dbPassword, int concurrentUsers) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        long startTime = System.currentTimeMillis();

        // List to store the response times for each request
        ConcurrentLinkedQueue<Long> responseTimes = new ConcurrentLinkedQueue<>();

        // Submit tasks to simulate concurrent users
        for (int i = 0; i < concurrentUsers; i++) {
            executor.submit(new LatencyTask(dbUrl, dbUser, dbPassword, responseTimes));
        }

        // Wait for all tasks to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to complete
        }

        // Calculate total elapsed time
        long endTime = System.currentTimeMillis();
        double totalTimeInSeconds = (endTime - startTime) / 1000.0;

        // Calculate the response times statistics
        long minResponseTime = responseTimes.stream().min(Long::compare).orElse(Long.MAX_VALUE);
        long maxResponseTime = responseTimes.stream().max(Long::compare).orElse(Long.MIN_VALUE);
        double averageResponseTime = responseTimes.stream().mapToLong(Long::longValue).average().orElse(0);

        // Print results
        System.out.println("Latency Results:");
        System.out.printf("Total Time: %.2f seconds\n", totalTimeInSeconds);
        System.out.printf("Total Requests: %d\n", concurrentUsers * REQUESTS_PER_USER);
        System.out.printf("Minimum Response Time: %d ms\n", minResponseTime);
        System.out.printf("Maximum Response Time: %d ms\n", maxResponseTime);
        System.out.printf("Average Response Time: %.2f ms\n", averageResponseTime);
    }

    // Task to simulate a user making requests
    static class LatencyTask implements Runnable {
        private String dbUrl;
        private String dbUser;
        private String dbPassword;
        private ConcurrentLinkedQueue<Long> responseTimes;

        public LatencyTask(String dbUrl, String dbUser, String dbPassword, ConcurrentLinkedQueue<Long> responseTimes) {
            this.dbUrl = dbUrl;
            this.dbUser = dbUser;
            this.dbPassword = dbPassword;
            this.responseTimes = responseTimes;
        }

        @Override
        public void run() {
            try (Connection connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword)) {
                connection.setAutoCommit(false);  // Start a transaction
                try (Statement statement = connection.createStatement()) {
                    for (int i = 0; i < REQUESTS_PER_USER; i++) {
                        long startTime = System.nanoTime();

                        // Execute SQL query (could be any type of query)
                        try (ResultSet resultSet = statement.executeQuery(SQL_QUERY)) {
                            while (resultSet.next()) {
                                // Process result if needed (for this test, we just measure the time)
                            }
                        }

                        long endTime = System.nanoTime();
                        long responseTime = (endTime - startTime) / 1_000_000;  // Convert to milliseconds
                        responseTimes.add(responseTime); // Store response time for later analysis
                    }
                    connection.commit();  // Commit transaction
                } catch (SQLException e) {
                    connection.rollback();  // Rollback in case of an error
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
