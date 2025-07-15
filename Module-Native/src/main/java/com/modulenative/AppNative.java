package com.modulenative;

import android.content.Context;

import androidx.annotation.Keep;

/**
 * <a href="https://github.com/yglx/protectSecretKeyDemo">...</a>
 * 通过JNI实现，三大保护策略
 * 第一：jni so天然的防普通的字节码破解
 * 第二：校验签名
 * 第三：偏移原始appId和appKey
 */
@Keep
public final class AppNative {
    static {
        System.loadLibrary("module_native");
    }

    /**
     * 获取appId和appKey
     * @param context ..
     * @return 结果为：appId\nappKey
     */
    @Keep
    public static native String appIdKey(Context context);

    /**
     * preSourceStringEncypt.gradle 里面 进行了AES/GCM/NoPadding 加密
     * 该函数，获取的反解密的key。然后通过key进行AES/GCM/NoPadding 解密
     * 参考掉用处实现。
     */
    @Keep
    public static native String strEk(Context context);

    /**
     * 在native，通过反射调用asset manager，读取文件原始text。
     * @param af asset的file path
     * @return assets的原文字
     */
    @Keep
    public static native String asts(Context c, String af);

    /**
     * 在native，通过反射调用asset manager，将文件进行解密到目标文件tp下。
     * @param af asset文件的file path
     * @param tp 目标文件路径
     * @return 是否成功
     */
    @Keep
    public static native boolean astf(Context c, String af, String tp);
}