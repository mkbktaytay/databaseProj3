import java.lang.management.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import com.sun.management.OperatingSystemMXBean;

public class SimpleSelect {

    // Connection parameters for PostgreSQL
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68";

    // Connection parameters for openGauss
    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@";

    // SQL Queries to execute
    private static final String SIMPLE_SELECT_QUERY = "SELECT name, city FROM customers;";
    private static final String INDEXED_SELECT_QUERY = "SELECT * FROM orders WHERE amount > 500;";
    private static final String JOIN_SELECT_QUERY = "SELECT c.name, o.amount FROM customers c JOIN orders o ON c.id = o.customer_id;";
    private static final String AGGREGATE_SELECT_QUERY = "SELECT SUM(amount) AS total_amount FROM orders;";

    private static final int ITERATIONS = 10;
    private static final long RESOURCE_PRINT_INTERVAL = 100; // 100ms for printing resource usage

    public static void main(String[] args) {
        try {
            // Test PostgreSQL and openGauss for each query
            testQueryOnDatabases(SIMPLE_SELECT_QUERY, "Simple SELECT");
            testQueryOnDatabases(INDEXED_SELECT_QUERY, "Indexed SELECT");
            testQueryOnDatabases(JOIN_SELECT_QUERY, "JOIN SELECT");
            testQueryOnDatabases(AGGREGATE_SELECT_QUERY, "Aggregate SELECT");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Method to test a specific query on both databases
    public static void testQueryOnDatabases(String query, String queryName) throws Exception {
        // Test PostgreSQL
        System.out.println("Testing " + queryName + " on PostgreSQL:");
        testQuery(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL", query, queryName);

        // Test openGauss
        System.out.println("Testing " + queryName + " on openGauss:");
        testQuery(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss", query, queryName);
    }

    // Method to test a specific query
    public static void testQuery(String url, String user, String password, String databaseName, String query, String queryName) throws Exception {
        double totalTime = 0;
        Class.forName("org.postgresql.Driver");

        // Get the system resource details before the query execution
        printSystemResourceUsage(queryName, "Before");

        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            for (int i = 1; i <= ITERATIONS; i++) {
                long startTime = System.nanoTime();

                // Monitor system resources every 0.1s while running the query
                long lastPrintTime = System.currentTimeMillis();
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery(query)) {

                    // Process the result set (use resultSet.next() to iterate)
                    while (resultSet.next()) {
                        // Here you can process each row if needed
                        // Example: resultSet.getString("name");

                        // Print system resources every 0.1s during the query execution
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - lastPrintTime >= RESOURCE_PRINT_INTERVAL) {
                            printSystemResourceUsage(queryName, "During Iteration " + i);
                            lastPrintTime = currentTime;
                        }
                    }
                }

                long endTime = System.nanoTime();
                double duration = (endTime - startTime) / 1_000_000_000.0; // Convert to seconds
                totalTime += duration;

                System.out.printf("%s Iteration %d: %.3f seconds%n", queryName, i, duration);
            }
        }

        // Get the system resource details after the query execution
        printSystemResourceUsage(queryName, "After");

        System.out.println("Average Execution Time for " + queryName + ": " + totalTime / ITERATIONS + " seconds\n");
    }

    // Method to print system resource usage (only CPU and Used Memory)
    public static void printSystemResourceUsage(String queryName, String when) {
        // Get CPU and memory usage details
        OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        // CPU usage
        double cpuLoad = osBean.getSystemCpuLoad() * 100; // Get system CPU load as percentage
        System.out.println(queryName + " - " + when + " - CPU Usage: " + cpuLoad + "%");

        // Memory usage (only used memory)
        long totalMemory = osBean.getTotalPhysicalMemorySize();
        long freeMemory = osBean.getFreePhysicalMemorySize();
        long usedMemory = totalMemory - freeMemory;
        System.out.println(queryName + " - " + when + " - Used Memory: " + usedMemory / (1024 * 1024) + " MB");
    }
}
