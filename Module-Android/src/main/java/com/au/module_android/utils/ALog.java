package com.au.module_android.utils;

import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * @author au
 * @date :2023/11/7 14:07
 * @description: Log日志
 */
public final class ALog {
    public static final String TAG = "androidAu";

    public static void d(@NonNull String s) {
        Log.d(TAG, s);
    }
    public static void w(@NonNull String s) {
        Log.w(TAG, s);
    }
    public static void e(@NonNull String s) {
        Log.e(TAG, s);
    }
    public static void t(@NonNull String s) {
        var curThread = Thread.currentThread();
        Log.d(TAG, String.format("thread(%d-%b) %s",
                curThread.getId(), curThread == Looper.getMainLooper().getThread(), s));
    }

    public static void d(@NonNull String tag, @NonNull String s) {
        Log.d(TAG, tag + ": " + s);
    }
    public static void w(@NonNull String tag, @NonNull String s) {
        Log.w(TAG, tag + ": " + s);
    }
    public static void e(@NonNull String tag, @NonNull String s) {
        Log.e(TAG, tag + ": " + s);
    }
    public static void t(@NonNull String tag, @NonNull String s) {
        var curThread = Thread.currentThread();
        Log.d(TAG, String.format("%s: thread(%d-%b) %s", tag,
                curThread.getId(), curThread == Looper.getMainLooper().getThread(), s));
    }

    public static void stace(@NonNull String s) {
        Log.d(TAG, s + "...start...");
        var ex = new Exception();
        ex.printStackTrace();
        Log.d(TAG, s + "...end!");
    }
}
