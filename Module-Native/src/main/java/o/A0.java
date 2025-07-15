package o;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;

import androidx.annotation.Keep;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Keep
public class A0 {
    private static final int DEFAULT_BUFFER_SIZE = 4096;
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
     * asset manager，将asset源文件af，进行解密到目标文件。
     * @param af assets文件名
     * @param tp 目标文件路径
     * @param k XOR 密钥
     * @return 是否成功
     */
    @Keep
    public static boolean fc(Context context, String af, String tp, String k) {
        if (af == null || k == null) return false;
        // 准备密钥字节
        byte[] kb = k.getBytes(StandardCharsets.UTF_8);
        // 确保输出文件的父目录存在
        File outFile = new File(tp);
        File parent = outFile.getParentFile();
        if (parent != null && !parent.exists() && !parent.mkdirs()) {
            return false;
        }

        AssetManager assetManager = context.getAssets();

        // 单一 try-with-resources 块，统一管理 InputStream 和 OutputStream
        try (InputStream is = assetManager.open(af);
             OutputStream os = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                // 只对本次读取的 bytesRead 个字节做 XOR
                for (int i = 0; i < bytesRead; i++) {
                    buffer[i] = (byte) (buffer[i] ^ kb[i % kb.length]);
                }
                os.write(buffer, 0, bytesRead);
            }
            // 确保数据刷出
            os.flush();
            return true;
        } catch (IOException e) {
            return false;
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
        byte[] kb = k.getBytes(StandardCharsets.UTF_8);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = s.read(buffer)) != -1) {
            // 只对本次读取的 bytesRead 个字节做 XOR
            for (int i = 0; i < bytesRead; i++) {
                buffer[i] = (byte) (buffer[i] ^ kb[i % kb.length]);
            }
            baos.write(buffer, 0, bytesRead);
        }
        // 最后整体转换为 UTF-8 字符串
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return baos.toString(StandardCharsets.UTF_8);
        } else {
            byte[] all = baos.toByteArray();
            return new String(all, StandardCharsets.UTF_8);
        }
    }

}
