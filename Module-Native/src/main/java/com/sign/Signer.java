package com.sign;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;

import androidx.annotation.Keep;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 通过自行故意命名来避免查看到该代码名字。
 */
@Keep
public final class Signer {
    /**
     * 获取app的签名sha1
     */
    @Keep
    public static String sha1(Context context) {
        var list = func(context, "SHA1");
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }
    
    @Keep
    public static String sha256(Context context) {
        var list = func(context, "SHA256");
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }

    @Keep
    public static String md5(Context context) {
        var list = func(context, "MD5");
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }

    private static List<String> func(Context context, String algo) {
        List<String> shals = new ArrayList<>();
        try {
            Signature[] signatures;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                var signingInfo = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNING_CERTIFICATES)
                        .signingInfo;
                signatures = signingInfo != null ? signingInfo.getApkContentsSigners() : null;
            } else {
                signatures = context.getPackageManager()
                        .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES)
                        .signatures;
            }

            if (signatures != null) {
                for (Signature signature : signatures) {
                    byte[] cert = signature.toByteArray();
                    MessageDigest md = MessageDigest.getInstance(algo);
                    byte[] publicKey = md.digest(cert);
                    StringBuilder hexString = new StringBuilder();

                    for (byte b : publicKey) {
                        String appendString = Integer.toHexString(0xFF & b).toUpperCase(Locale.US);
                        if (appendString.length() == 1) {
                            hexString.append("0");
                        }
                        hexString.append(appendString).append(":");
                    }

                    String result = hexString.toString();
                    String sha1 = result.substring(0, result.length() - 1);
                    shals.add(sha1);
                }
            }
            return shals;
        } catch (NoSuchAlgorithmException | PackageManager.NameNotFoundException | NullPointerException e) {
            return shals;
        }
    }
}
