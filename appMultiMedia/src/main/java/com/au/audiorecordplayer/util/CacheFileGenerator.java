package com.au.audiorecordplayer.util;

import com.au.module_android.Globals;

import java.io.File;

public class CacheFileGenerator {
    public static String generateCacheFilePath(String prefix, String extension) {
        return cacheFilePath() + File.separator + prefix + System.currentTimeMillis() + extension;
    }

    public static String cacheFilePath() {
        var filePath = Globals.INSTANCE.getGoodCacheDir().getAbsolutePath() + File.separator + "audio";
        var f = new File(filePath);
        if (!f.exists()) {
            f.mkdirs();
        }
        return filePath;
    }
}
