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
    public static native String appIdAndKey(Context context);

    @Keep
    public static native String stringEncryptSecret(Context context);
}