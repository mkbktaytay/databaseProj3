import java.io.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Recovery {

    // PostgreSQL 和 openGauss 数据库连接参数
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68";

    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@";

    // 模拟故障命令：停止数据库
    private static final String STOP_POSTGRESQL_CMD = "net stop postgresql-x64-13";  // PostgreSQL 服务名请根据实际情况修改
    private static final String STOP_OPENGAUSS_CMD = "net stop gaussdb";      // openGauss 服务名请根据实际情况修改

    // 启动数据库命令
    private static final String START_POSTGRESQL_CMD = "net start postgresql-x64-13";  // PostgreSQL 服务名请根据实际情况修改
    private static final String START_OPENGAUSS_CMD = "net start gaussdb";      // openGauss 服务名请根据实际情况修改

    public static void main(String[] args) {
        try {
            // 测试 PostgreSQL 恢复时间
            System.out.println("Testing PostgreSQL Recovery Time:");
            for (int i = 1; i <= 10; i++) {
                System.out.println("\nTest #" + i);
                testRecoveryTime("PostgreSQL", STOP_POSTGRESQL_CMD, START_POSTGRESQL_CMD);
            }

            // 测试 openGauss 恢复时间
            System.out.println("\nTesting openGauss Recovery Time:");
            for (int i = 1; i <= 10; i++) {
                System.out.println("\nTest #" + i);
                testRecoveryTime("openGauss", STOP_OPENGAUSS_CMD, START_OPENGAUSS_CMD);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 测试恢复时间
    public static void testRecoveryTime(String databaseName, String stopCommand, String startCommand) throws Exception {
        // 1. 模拟故障 (停止数据库)
        System.out.println("Simulating fault...");
        long crashTime = simulateFault(stopCommand);

        // 2. 重启数据库 (触发恢复)
        long recoveryTime = restartDatabase(startCommand);

        // 3. 计算恢复时间
        long recoveryDuration = recoveryTime - crashTime;

        // 4. 输出结果
        System.out.println("Recovery Time for " + databaseName + ": " + recoveryDuration + " milliseconds");
    }

    // 模拟数据库故障 (停止数据库服务)
    public static long simulateFault(String stopCommand) throws Exception {
        // 记录故障发生时间
        long crashTime = System.currentTimeMillis();
        System.out.println("Fault occurred at: " + formatTime(crashTime));

        // 执行停止数据库服务的命令
        runCommand(stopCommand);

        // 模拟数据库故障期间的停机时间，等待 5 秒钟
        Thread.sleep(0); // 模拟 5 秒的故障停机时间

        return crashTime;
    }

    // 重启数据库以触发恢复
    public static long restartDatabase(String startCommand) throws Exception {
        // 记录恢复开始时间
        long recoveryTime = System.currentTimeMillis();
        System.out.println("Recovery started at: " + formatTime(recoveryTime));

        // 执行启动数据库服务的命令
        runCommand(startCommand);

        // 模拟数据库恢复时间，等待 3 秒
        Thread.sleep(3000); // 模拟数据库恢复时间

        return recoveryTime;
    }

    // 执行系统命令（例如停止或启动数据库）
    public static void runCommand(String command) throws IOException {
        Process process = Runtime.getRuntime().exec(command);
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // 格式化时间为人类可读的形式
    public static String formatTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        return sdf.format(new Date(timestamp));
    }
}
