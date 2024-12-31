import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Authentication {

    // PostgreSQL 和 openGauss 数据库连接参数
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68";  // 替换为正确的密码

    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@";  // 替换为正确的密码

    public static void main(String[] args) {
        try {
            // 测试 PostgreSQL
            System.out.println("=== 测试 PostgreSQL ===");
            testPasswordAuthentication(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL");
            testKerberosAuthentication(POSTGRESQL_URL, "PostgreSQL");
            testSSLAuthentication(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL");

            // 测试 openGauss
            System.out.println("\n=== 测试 openGauss ===");
            testPasswordAuthentication(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss");
            testKerberosAuthentication(OPENGAUSS_URL, "openGauss");
            testSSLAuthentication(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 测试密码认证
    public static void testPasswordAuthentication(String jdbcUrl, String user, String password, String dbName) throws SQLException {
        System.out.println("\n--- " + dbName + " - 测试密码认证 ---");

        // 正确的用户名和密码
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            System.out.println(dbName + " 密码认证成功，连接数据库!");
        } catch (SQLException e) {
            System.out.println(dbName + " 密码认证失败: " + e.getMessage());
        }

        // 错误的用户名和密码
        try (Connection conn = DriverManager.getConnection(jdbcUrl, "wronguser", "wrongpassword")) {
            System.out.println(dbName + " 密码认证成功，连接数据库!（错误用户名密码）");
        } catch (SQLException e) {
            System.out.println(dbName + " 密码认证失败（错误用户名密码）：" + e.getMessage());
        }
    }

    // 测试Kerberos认证
    public static void testKerberosAuthentication(String jdbcUrl, String dbName) throws SQLException {
        System.out.println("\n--- " + dbName + " - 测试Kerberos认证 ---");

        // 使用Kerberos认证连接数据库
        // 你需要确保Kerberos环境已配置，并且数据库启用了Kerberos认证。
        // 下面的代码假设已经配置了Kerberos和JDBC相关设置

        String kerberosJdbcUrl = jdbcUrl + "?gssEncMode=disable&gssServiceName=postgres";
        Properties props = new Properties();
        props.setProperty("user", "user@REALM");  // Kerberos用户名
        props.setProperty("password", "");

        try (Connection conn = DriverManager.getConnection(kerberosJdbcUrl, props)) {
            System.out.println(dbName + " Kerberos认证成功，连接数据库!");
        } catch (SQLException e) {
            System.out.println(dbName + " Kerberos认证失败: " + e.getMessage());
        }
    }

    // 测试SSL/TLS认证
    public static void testSSLAuthentication(String jdbcUrl, String user, String password, String dbName) throws SQLException {
        System.out.println("\n--- " + dbName + " - 测试SSL/TLS认证 ---");

        // 确保数据库启用了SSL，连接URL需要指定使用SSL
        String sslJdbcUrl = jdbcUrl + "?ssl=true&sslmode=require";  // 需要数据库启用SSL

        try (Connection conn = DriverManager.getConnection(sslJdbcUrl, user, password)) {
            System.out.println(dbName + " SSL/TLS认证成功，连接数据库!");
        } catch (SQLException e) {
            System.out.println(dbName + " SSL/TLS认证失败: " + e.getMessage());
        }
    }
}
