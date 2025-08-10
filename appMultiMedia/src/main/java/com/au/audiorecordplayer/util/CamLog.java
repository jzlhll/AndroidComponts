package com.au.audiorecordplayer.util;

import android.util.Log;

import com.au.audiorecordplayer.BuildConfig;

public class CamLog {
    private static final String TAG = "au_cam";

    public static void d(String s) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, s);
        } else {
            Log.d(TAG, s);
        }
    }

    public static void e(String s) {
        Log.e(TAG, s);
    }

    public static void d(String tag, String s) {
        if (BuildConfig.DEBUG) {
            Log.w(TAG, tag + ": " + s);
        } else {
            Log.d(TAG, tag + ": " + s);
        }
    }

    public static void e(String tag, String s) {
        Log.e(TAG, tag + ": " + s);
    }
}
