package com.au.module_android.utils

import android.os.Looper
import android.util.Log
import com.au.module.android.BuildConfig

const val TAG:String = "au_log"
var hasFileLog = false

inline fun loge(block:()->String) {
    val str = block()
    Log.e(TAG, str)
    if (hasFileLog) {
        FileLog.write("E $TAG: $str", true)
    }
}

inline fun logw(block:()->String) {
    val str = block()
    Log.w(TAG, block())
    if (hasFileLog) {
        FileLog.write("W $TAG: $str")
    }
}

inline fun logd(block:()->String) {
    if (BuildConfig.DEBUG) {
        val str = block()
        Log.d(TAG, str)
        if (hasFileLog) {
            FileLog.write("D $TAG: $str")
        }
    }
}

inline fun logt(block:()->String) {
    if(BuildConfig.DEBUG) {
        val isMainThread = (Thread.currentThread().id == Looper.getMainLooper().thread.id)
        Log.d(TAG, "thread${Thread.currentThread().id}-$isMainThread: " + block())
    }
}

fun logStace(s: String) {
    Log.d(TAG, "$s...start...")
    val ex = Exception()
    ex.printStackTrace()
    Log.d(TAG, "$s...end!")
}