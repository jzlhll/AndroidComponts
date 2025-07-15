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
    public static native String ik(Context context);

    /**
     * preSourceStringEncypt.gradle 里面 进行了AES/GCM/NoPadding 加密
     * 该函数，获取的反解密的key。然后通过key进行AES/GCM/NoPadding 解密
     * 参考掉用处实现。
     */
    @Keep
    public static native String ses(Context context);

    /**
     * 在native，通过反射调用asset manager，将参数af
     * @param af asset的file path
     * @return assets的原文字
     */
    @Keep
    public static native String t1(Context c, String af);
}