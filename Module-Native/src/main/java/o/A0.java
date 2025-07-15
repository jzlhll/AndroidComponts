package o;

import android.content.Context;
import android.content.res.AssetManager;

import androidx.annotation.Keep;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Keep
public class A0 {
//    public static final String XOR_KEY = "allan123"; // 建议更复杂的密钥

    /**
     * XOR 加密/解密核心逻辑（对称）
     */
    @Keep
    private static void x(byte[] data, byte[] key) {
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte)(data[i] ^ key[i % key.length]);
        }
    }

    //加密文件
    //    public static void encryptFile(File sourceFile, File targetFile) throws IOException {
//        c(sourceFile, targetFile, XOR_KEY);
//    }

    /**
     * 解密文件（其实就是再次 XOR）
     * @param ef 加密文件
     * @param df 解密文件
     * @param k  密钥
     */
    @Keep
    public static void dc(File ef, File df, String k) throws IOException {
        c(ef, df, k);
    }

    /**
     * 统一处理函数（加密/解密）
     */
    @Keep
    private static void c(File sourceFile, File targetFile, String key) throws IOException {
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        try (
                InputStream is = new FileInputStream(sourceFile);
                OutputStream os = new FileOutputStream(targetFile)
        ) {
            byte[] buffer = new byte[2048]; //与assetsEncryptRules.gradle脚本一致
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                byte[] actualData = (bytesRead == buffer.length) ? buffer : Arrays.copyOf(buffer, bytesRead);
                x(actualData, keyBytes);
                os.write(actualData);
            }
        }
    }

    /**
     * 从 assets 读取文件，使用 XOR 解密为字符串 文本文件一次，直接读完，并返回String。
     * @param c  Context
     * @param af assets 文件名
     * @param k  XOR 密钥
     * @return 解密后的字符串，失败返回 null
     */
    @Keep
    public static String t1(Context c, String af, String k) {
        if (af == null) return null;

        AssetManager assetManager = c.getAssets();
        try (InputStream is = assetManager.open(af)) {
            return os(is, k);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 将输入流中的字节读取并 XOR 解密为字符串
     *
     * @param s InputStream 数据源
     * @param k 密钥
     * @return 解密后的字符串
     */
    @Keep
    private static String os(InputStream s, String k) throws IOException {
        byte[] data = rb(s);
        x(data, k.getBytes(StandardCharsets.UTF_8));
        return new String(data, StandardCharsets.UTF_8);
    }

    /**
     * 读取 InputStream 中所有字节
     */
    @Keep
    private static byte[] rb(InputStream i) throws IOException {
        byte[] bf = new byte[4096];
        int c;
        try (ByteArrayOutputStream b = new ByteArrayOutputStream()) {
            while ((c = i.read(bf)) != -1) {
                b.write(bf, 0, c);
            }
            return b.toByteArray();
        }
    }

}
