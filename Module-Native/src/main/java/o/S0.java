package o;

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
public final class S0 {
    /**
     * 获取app的签名sha1
     */
    @Keep
    public static String s1(Context context) {
        var list = c0(context, "SHA1");
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }

    @Keep
    public static String s6(Context context) {
        var list = c0(context, "SHA256");
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }

    @Keep
    public static String m5(Context context) {
        var list = c0(context, "MD5");
        return (list != null && !list.isEmpty()) ? list.get(0) : "";
    }

    private static List<String> c0(Context context, String algo) {
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
