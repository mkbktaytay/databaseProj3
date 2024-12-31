import java.io.*;
import java.nio.file.*;
import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Restore {

    // PostgreSQL 和 openGauss 数据库连接参数
    private static final String POSTGRESQL_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String POSTGRESQL_USER = "postgres";
    private static final String POSTGRESQL_PASSWORD = "cglcgl68";

    private static final String OPENGAUSS_URL = "jdbc:postgresql://localhost:15432/postgres";
    private static final String OPENGAUSS_USER = "gaussdb";
    private static final String OPENGAUSS_PASSWORD = "Louyibin123@";

    // 密钥文件路径（假设你有一个加密密钥文件）
    private static final String ENCRYPTION_KEY_FILE = "C:\\Users\\cxlou\\Desktop\\databaseProj3\\src\\encryption.key"; // Windows路径

    // 备份文件路径
    private static final String BACKUP_FILE_PATH = "C:\\Users\\cxlou\\Desktop\\databaseProj3\\src\\backup_data.sql"; // Windows路径

    public static void main(String[] args) {
        try {
            // 1. 生成 AES 密钥并保存到文件
            System.out.println("=== 生成 AES 密钥 ===");
            generateAndSaveEncryptionKey(ENCRYPTION_KEY_FILE);

            // 2. 备份并加密数据
            System.out.println("=== 测试备份数据加密 ===");
            backupAndEncryptData(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL");
            backupAndEncryptData(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss");

            // 3. 恢复数据并验证安全性
            System.out.println("\n=== 测试恢复数据的安全性 ===");
            restoreAndVerifyData(POSTGRESQL_URL, POSTGRESQL_USER, POSTGRESQL_PASSWORD, "PostgreSQL");
            restoreAndVerifyData(OPENGAUSS_URL, OPENGAUSS_USER, OPENGAUSS_PASSWORD, "openGauss");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 生成并保存 AES 密钥文件
    public static void generateAndSaveEncryptionKey(String keyFilePath) throws Exception {
        // 生成 AES 密钥
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128); // 使用 128 位密钥
        SecretKey secretKey = keyGenerator.generateKey();

        // 将密钥保存到文件
        try (FileOutputStream fos = new FileOutputStream(keyFilePath)) {
            fos.write(secretKey.getEncoded());  // 将密钥的字节写入文件
        }

        System.out.println("密钥文件已生成并保存到: " + keyFilePath);
    }

    // 备份并加密数据
    public static void backupAndEncryptData(String jdbcUrl, String user, String password, String dbName) throws Exception {
        System.out.println(dbName + " - 备份数据并加密");

        // 1. 执行数据库备份命令（以 PostgreSQL 为例）
        String backupCommand = "pg_dump -U " + user + " -h localhost -F c -b -v -f " + BACKUP_FILE_PATH + " your_database";
        runCommand(backupCommand);

        // 2. 加密备份文件
        encryptBackupFile(BACKUP_FILE_PATH, ENCRYPTION_KEY_FILE);

        System.out.println(dbName + " 数据备份并加密成功！");
    }

    // 加密备份文件
    public static void encryptBackupFile(String backupFilePath, String keyFilePath) throws Exception {
        // 读取密钥文件
        byte[] keyBytes = Files.readAllBytes(Paths.get(keyFilePath));
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        // 加密
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);

        // 读取备份文件内容
        byte[] fileBytes = Files.readAllBytes(Paths.get(backupFilePath));

        // 加密文件内容
        byte[] encryptedBytes = cipher.doFinal(fileBytes);

        // 保存加密后的文件
        try (FileOutputStream fos = new FileOutputStream(backupFilePath + ".enc")) {
            fos.write(encryptedBytes);
        }

        System.out.println("备份文件已加密为: " + backupFilePath + ".enc");
    }

    // 恢复并验证数据的安全性
    public static void restoreAndVerifyData(String jdbcUrl, String user, String password, String dbName) throws Exception {
        System.out.println(dbName + " - 恢复数据并验证安全性");

        // 1. 解密备份文件
        decryptBackupFile(BACKUP_FILE_PATH + ".enc", ENCRYPTION_KEY_FILE);

        // 2. 恢复数据
        String restoreCommand = "pg_restore -U " + user + " -h localhost -d your_database " + BACKUP_FILE_PATH;
        runCommand(restoreCommand);

        // 3. 验证数据完整性
        verifyDataIntegrity(jdbcUrl, user, password, dbName);

        System.out.println(dbName + " 数据恢复并验证安全性成功！");
    }

    // 解密备份文件
    public static void decryptBackupFile(String encryptedFilePath, String keyFilePath) throws Exception {
        // 读取密钥文件
        byte[] keyBytes = Files.readAllBytes(Paths.get(keyFilePath));
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");

        // 解密
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);

        // 读取加密的备份文件
        byte[] encryptedBytes = Files.readAllBytes(Paths.get(encryptedFilePath));

        // 解密文件内容
        byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

        // 保存解密后的文件
        try (FileOutputStream fos = new FileOutputStream(BACKUP_FILE_PATH)) {
            fos.write(decryptedBytes);
        }

        System.out.println("备份文件已解密为: " + BACKUP_FILE_PATH);
    }

    // 验证数据完整性（简单检查数据是否一致）
    public static void verifyDataIntegrity(String jdbcUrl, String user, String password, String dbName) throws SQLException {
        System.out.println(dbName + " - 验证数据完整性");

        // 连接数据库，查询并验证数据
        try (Connection conn = DriverManager.getConnection(jdbcUrl, user, password)) {
            String query = "SELECT COUNT(*) FROM customers";  // 假设你有一个表用于验证数据
            try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    System.out.println(dbName + " 数据表行数: " + count);
                    // 如果行数与预期匹配，说明数据恢复成功
                }
            }
        }
    }

    // 执行系统命令（备份、恢复等）
    public static void runCommand(String command) throws IOException {
        // 执行 Windows 命令（使用 CMD 命令行）
        Process process = Runtime.getRuntime().exec(new String[]{"cmd", "/c", command});
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
