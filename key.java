import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;

public class key {

    public static void main(String[] args) {
        try {
            // 生成 AES 密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(128); // 使用 128 位密钥
            SecretKey secretKey = keyGenerator.generateKey();

            // 将密钥保存到文件中
            saveKeyToFile("C:\\Users\\cxlou\\Desktop\\databaseProj3\\src\\encryption.key", secretKey); // 替换为你希望保存的路径

            System.out.println("密钥文件已生成！");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 将密钥保存到文件
    public static void saveKeyToFile(String filePath, SecretKey secretKey) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(secretKey.getEncoded());  // 将密钥的字节写入文件
        }
    }
}