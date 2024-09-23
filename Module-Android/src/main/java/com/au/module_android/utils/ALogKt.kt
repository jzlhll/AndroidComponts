package com.au.module_android.utils

import android.os.Looper
import android.util.Log
import com.au.module.android.BuildConfig
import java.util.Locale

const val TAG:String = "au_log"

inline fun loge(tag:String = TAG, block:()->String) {
    val str = block()
    Log.e(tag, str)
    if (BuildConfig.ENABLE_FILE_LOG) {
        FileLog.write("E $tag: $str", true)
    }
}

inline fun logw(tag:String = TAG, block:()->String) {
    val str = block()
    Log.w(tag, str)
    if (BuildConfig.ENABLE_FILE_LOG) {
        FileLog.write("W $tag: $str")
    }
}

inline fun logd(tag:String = TAG, canHasFileLog:Boolean = true, block:()->String) {
    if (BuildConfig.DEBUG) {
        val str = block()
        Log.d(tag, str)
        if (BuildConfig.ENABLE_FILE_LOG && canHasFileLog) {
            FileLog.write("D $tag: $str")
        }
    }
}

inline fun logt(tag:String = TAG, block:()->String) {
    if(BuildConfig.DEBUG) {
        val isMainThread = (Thread.currentThread().id == Looper.getMainLooper().thread.id)
        val str = block()
        val log = if (isMainThread) {
            "MainThread: $str"
        } else {
            String.format(Locale.ROOT, "SubThread[%02d]: %s", Thread.currentThread().id, str)
        }
        Log.d(tag, log)
    }
}

fun logStace(tag:String = TAG, s: String) {
    Log.d(tag, "$s...start...")
    val ex = Exception()
    ex.printStackTrace()
    Log.d(tag, "$s...end!")
}