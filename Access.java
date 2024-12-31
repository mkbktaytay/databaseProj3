import java.sql.*;

public class Access {

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
            System.out.println("=== 测试 PostgreSQL 访问控制 ===");
            testRBAC(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL");
            testFineGrainedAccessControl(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL");

            // 测试 openGauss
            System.out.println("\n=== 测试 openGauss 访问控制 ===");
            testRBAC(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss");
            testFineGrainedAccessControl(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 测试基于角色的访问控制（RBAC）
    public static void testRBAC(String jdbcUrl, String user, String password, String dbName) throws SQLException {
        System.out.println("\n--- " + dbName + " - 测试基于角色的访问控制（RBAC） ---");

        // 角色1：有权限访问数据的用户
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            System.out.println(dbName + " 角色 " + user + " 成功连接数据库，验证数据访问权限！");
            String query = "SELECT * FROM customers"; // 查询 customers 表
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                System.out.println(dbName + " 用户 " + user + " 查询数据成功！");
            } catch (SQLException e) {
                System.out.println(dbName + " 用户 " + user + " 查询数据失败！" + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(dbName + " 用户 " + user + " 连接失败！" + e.getMessage());
        }

        // 角色2：没有权限访问数据的用户
        try (Connection conn = DriverManager.getConnection(jdbcUrl, "no_access_user", "password")) {
            System.out.println(dbName + " 角色 'no_access_user' 尝试连接数据库...");
            String query = "SELECT * FROM customers"; // 查询 customers 表
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                System.out.println(dbName + " 用户 'no_access_user' 查询数据成功！");
            } catch (SQLException e) {
                System.out.println(dbName + " 用户 'no_access_user' 查询数据失败！" + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(dbName + " 用户 'no_access_user' 连接失败！" + e.getMessage());
        }
    }

    // 测试细粒度访问控制（例如，基于行或列的权限控制）
    public static void testFineGrainedAccessControl(String jdbcUrl, String user, String password, String dbName) throws SQLException {
        System.out.println("\n--- " + dbName + " - 测试细粒度访问控制 ---");

        // 假设某些用户只能访问特定的列
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            System.out.println(dbName + " 角色 " + user + " 成功连接数据库，验证数据访问权限！");
            String query = "SELECT city FROM customers"; // 仅查询客户的城市列（假设某些用户只能访问该列）
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                System.out.println(dbName + " 用户 " + user + " 查询城市数据成功！");
            } catch (SQLException e) {
                System.out.println(dbName + " 用户 " + user + " 查询城市数据失败！" + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(dbName + " 用户 " + user + " 连接失败！" + e.getMessage());
        }

        // 假设某些用户只能访问特定的行
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            System.out.println(dbName + " 角色 " + user + " 成功连接数据库，验证行级访问控制！");
            String query = "SELECT * FROM customers WHERE city = 'New York'"; // 假设某些用户只能查询城市为 'New York' 的行
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                System.out.println(dbName + " 用户 " + user + " 查询行数据成功！");
            } catch (SQLException e) {
                System.out.println(dbName + " 用户 " + user + " 查询行数据失败！" + e.getMessage());
            }
        } catch (SQLException e) {
            System.out.println(dbName + " 用户 " + user + " 连接失败！" + e.getMessage());
        }
    }
}
