package com.au.audiorecordplayer.util;

import com.au.module_android.Globals;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class FileUtil {
    public static void delete(File file) {
        if (file != null && file.exists()) {
            file.delete();
        }
    }

    public static String longTimeToStr(long ts) {
        var format = new SimpleDateFormat("MMdd_hhmm_sss", Locale.getDefault());
        return format.format(ts);
    }

    public static String getNextRecordFilePath(String extension) {
        var dir = Globals.INSTANCE.getGoodFilesDir().getAbsolutePath() + "/record/";
        var f = new File(dir);
        if (!f.exists()) {
            f.mkdirs();
        }
        return Globals.INSTANCE.getGoodFilesDir().getAbsolutePath() + "/record/VID_" + longTimeToStr(System.currentTimeMillis()) + extension;
    }
}
