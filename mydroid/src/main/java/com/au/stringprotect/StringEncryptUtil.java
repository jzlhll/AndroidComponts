package com.au.stringprotect;

import android.util.Log;

import androidx.annotation.NonNull;

import com.au.module_android.Globals;
import com.modulenative.AppNative;

import java.nio.charset.StandardCharsets;

public class StringEncryptUtil {
    private static final String SECRET_KEY_STR = AppNative.stringEncryptSecret(Globals.internalApp);
    private static final byte[] SECRET_KEY = SECRET_KEY_STR.getBytes(StandardCharsets.UTF_8);

    @NonNull
    public static String decrypt(String encoded) {
        //自行将保存的密钥进行提取和解析。
        try {
            Log.d("au", "decrypt " + SECRET_KEY_STR);
            return AESGCMUtil.decrypt(encoded, SECRET_KEY);
        } catch (Exception e) {
            //
        }
        return "";
    }
}
