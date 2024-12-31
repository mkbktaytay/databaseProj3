import java.sql.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Integrity {

    // PostgreSQL 和 openGauss 数据库连接参数
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68";

    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@";

    // 目标表和数据
    private static final String TEST_TABLE = "customers";
    private static final String TEST_INSERT_SQL = "INSERT INTO " + TEST_TABLE + " (name, city) VALUES (?, ?)";
    private static final String TEST_SELECT_SQL = "SELECT * FROM " + TEST_TABLE + " WHERE name = ? AND city = ?";

    // 用于验证的数据
    private static final String TEST_NAME = "John Doe";
    private static final String TEST_CITY = "New York";

    public static void main(String[] args) {
        try {
            // 测试 PostgreSQL 数据完整性验证
            System.out.println("Testing PostgreSQL Data Integrity Check:");
            testDataIntegrity("PostgreSQL", POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD);

            // 测试 openGauss 数据完整性验证
            System.out.println("\nTesting openGauss Data Integrity Check:");
            testDataIntegrity("openGauss", OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 测试数据完整性验证
    public static void testDataIntegrity(String databaseName, String url, String user, String password) throws Exception {
        // 1. 在恢复前插入数据
        try (Connection connection = DriverManager.getConnection(url, user, password)) {
            insertTestData(connection);
        }

        // 2. 模拟故障和恢复
        long crashTime = simulateFault();
        long recoveryTime = restartDatabase();

        // 3. 数据完整性验证
        long startTime = System.currentTimeMillis();
        boolean dataIntegrity = checkDataIntegrity(url, user, password);
        long endTime = System.currentTimeMillis();

        // 4. 记录数据完整性验证所需时间
        long dataIntegrityCheckTime = endTime - startTime;
        System.out.println("Data Integrity Check Time for " + databaseName + ": " + dataIntegrityCheckTime + " milliseconds");

        // 5. 输出数据完整性验证结果
        if (dataIntegrity) {
            System.out.println("Data integrity verified successfully.");
        } else {
            System.out.println("Data integrity verification failed! Data might be lost or corrupted.");
        }
    }

    // 插入测试数据
    public static void insertTestData(Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(TEST_INSERT_SQL)) {
            stmt.setString(1, TEST_NAME);
            stmt.setString(2, TEST_CITY);
            stmt.executeUpdate();
        }
    }

    // 模拟故障：停止数据库
    public static long simulateFault() throws Exception {
        long crashTime = System.currentTimeMillis();
        System.out.println("Fault occurred at: " + formatTime(crashTime));

        // 模拟故障，假设数据库已经停止
        Thread.sleep(5000); // 假设数据库停机 5 秒
        return crashTime;
    }

    // 重启数据库以触发恢复
    public static long restartDatabase() throws Exception {
        long recoveryTime = System.currentTimeMillis();
        System.out.println("Recovery started at: " + formatTime(recoveryTime));

        // 模拟数据库恢复
        Thread.sleep(3000); // 假设数据库恢复需要 3 秒
        return recoveryTime;
    }

    // 检查数据完整性：查询插入的数据是否恢复
    public static boolean checkDataIntegrity(String url, String user, String password) throws SQLException {
        boolean dataFound = false;

        // 检查数据库中是否能找到插入的记录
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement stmt = connection.prepareStatement(TEST_SELECT_SQL)) {
            stmt.setString(1, TEST_NAME);
            stmt.setString(2, TEST_CITY);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    // 找到了数据，验证数据完整性
                    String name = rs.getString("name");
                    String city = rs.getString("city");

                    // 检查数据是否一致
                    if (TEST_NAME.equals(name) && TEST_CITY.equals(city)) {
                        dataFound = true;
                    }
                }
            }
        }

        return dataFound;
    }

    // 格式化时间为人类可读的形式
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date(timestamp));
    }
}
