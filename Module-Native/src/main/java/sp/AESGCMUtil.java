package sp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESGCMUtil {
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int TAG_LENGTH_BIT = 128; // GCM认证标签长度（固定128位）
    private static final int IV_LENGTH_BYTE = 12;   // 推荐IV长度（12字节）
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * 加密数据
     * @param plaintext 明文
     * @param key 密钥（16/24/32字节对应AES-128/192/256）
     * @return Base64编码的字符串，格式为：IV + 密文
     */
    public static String encrypt(String plaintext, byte[] key) throws Exception {
        // 生成随机IV（12字节）
        byte[] iv = new byte[IV_LENGTH_BYTE];
        SECURE_RANDOM.nextBytes(iv);
        
        // 创建密钥对象
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        
        // 初始化加密器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        
        // 执行加密
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // 组合IV和密文：IV + 密文
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 解密数据
     * @param encryptedData Base64编码的加密数据（IV + 密文）
     * @param key 密钥（必须与加密时使用的相同）
     * @return 解密后的原始字符串
     */
    public static String decrypt(String encryptedData, byte[] key) throws Exception {
        // 解码Base64
        byte[] combined = Base64.getDecoder().decode(encryptedData);
        
        // 提取IV（前12字节）
        byte[] iv = new byte[IV_LENGTH_BYTE];
        System.arraycopy(combined, 0, iv, 0, iv.length);
        
        // 提取密文（剩余部分）
        byte[] ciphertext = new byte[combined.length - IV_LENGTH_BYTE];
        System.arraycopy(combined, IV_LENGTH_BYTE, ciphertext, 0, ciphertext.length);
        
        // 创建密钥对象
        SecretKey secretKey = new SecretKeySpec(key, "AES");
        
        // 初始化解密器
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));
        
        // 执行解密
        byte[] plaintext = cipher.doFinal(ciphertext);
        
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}