package com.au.audiorecordplayer.util;

import android.util.Log;

import com.au.module_android.BuildConfig;
import com.au.module_android.utils.ALogJ;

public class MyLog {
    private static final String TAG = "au";
    private static final boolean DEBUG_LEVEL_E = true;
    //如果你希望降低debug日志；则改第一位true为false
    private static final boolean DEBUG_LEVEL_W = true && DEBUG_LEVEL_E && BuildConfig.DEBUG;
    //如果你希望降低debug日志；则改第一位true为false
    private static final boolean DEBUG_LEVEL_D = true && DEBUG_LEVEL_E && DEBUG_LEVEL_W && BuildConfig.DEBUG;
    //由于很多手机d级别以下日志太多，调试的时候，我们改为true，将d改成w
    private static final boolean CONVERT_LEVEL_D_TO_W = true && BuildConfig.DEBUG;

    public static void d(String tag, String log) {
        if(DEBUG_LEVEL_D) {
            if (CONVERT_LEVEL_D_TO_W) {
                Log.w(TAG, tag + ":" + log);
            } else {
                Log.d(TAG, tag + ":" + log);
            }
        }
    }

    public static void e(String tag, String log) {
        if(DEBUG_LEVEL_E) Log.e(TAG, tag + ":" + log);
    }

    public static void w(String tag, String log) {
        if(DEBUG_LEVEL_W) Log.w(TAG, tag + ":" + log);
    }

    public static void d(String log) {
        if(DEBUG_LEVEL_D) {
            if (CONVERT_LEVEL_D_TO_W) {
                Log.w(TAG, log);
            } else {
                Log.d(TAG, log);
            }
        }
    }

    public static void e(String log) {
        if(DEBUG_LEVEL_E) Log.e(TAG, log);
    }

    public static void w(String log) {
        if(DEBUG_LEVEL_W) Log.w(TAG, log);
    }
    
    public static void ex(Throwable e) {
        e(ALogJ.ex(e));
    }
}
