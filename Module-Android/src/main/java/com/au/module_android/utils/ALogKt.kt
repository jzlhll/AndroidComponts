package com.au.module_android.utils

import android.os.Looper
import android.util.Log
import com.au.module_android.BuildConfig
import java.util.Locale

const val TAG:String = "au_log"
const val ALWAYS_LOG = false
const val ALWAYS_FILE_LOG = false

inline fun loge(tag:String = TAG, block:()->String) {
    val str = block()
    Log.e(tag, str)
    if (BuildConfig.ENABLE_FILE_LOG || ALWAYS_FILE_LOG) {
        FileLog.write("E $tag: $str", true)
    }
}

inline fun logw(tag:String = TAG, block:()->String) {
    val str = block()
    Log.w(tag, str)
    if (BuildConfig.ENABLE_FILE_LOG || ALWAYS_FILE_LOG) {
        FileLog.write("W $tag: $str")
    }
}

inline fun logwNoFile(tag:String = TAG, block:()->String) {
    val str = block()
    Log.w(tag, str)
}

inline fun logd(tag:String = TAG, block:()->String) {
    if (BuildConfig.DEBUG || ALWAYS_LOG) {
        val str = block()
        Log.d(tag, str)
        if (BuildConfig.ENABLE_FILE_LOG || ALWAYS_FILE_LOG) {
            FileLog.write("D $tag: $str")
        }
    }
}

inline fun logdNoFile(tag:String = TAG, block:()->String) {
    if (BuildConfig.DEBUG || ALWAYS_LOG) {
        val str = block()
        Log.d(tag, str)
    }
}

inline fun logt(tag:String = TAG, block:()->String) {
    if(BuildConfig.DEBUG || ALWAYS_LOG) {
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