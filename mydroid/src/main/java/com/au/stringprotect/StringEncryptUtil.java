package com.au.stringprotect;

import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;

public class StringEncryptUtil {
    private static final byte[] SECRET_KEY = "abcde12345hijklm".getBytes(StandardCharsets.UTF_8);

    @NonNull
    public static String decrypt(String encoded) {
        //自行将保存的密钥进行提取和解析。
        try {
            return AESGCMUtil.decrypt(encoded, SECRET_KEY);
        } catch (Exception e) {
            //
        }
        return "";
    }
}
