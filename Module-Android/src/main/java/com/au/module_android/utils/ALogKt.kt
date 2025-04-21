package com.au.module_android.utils

import android.os.Looper
import android.util.Log
import com.au.module_android.BuildConfig
import java.util.Locale

const val TAG:String = "au_log"
const val ALWAYS_LOG = false
const val ALWAYS_FILE_LOG = false

inline fun <THIS : Any> THIS.loge(tag:String = TAG, crossinline block: (THIS) -> String) {
    val str = block(this)
    val className = this.javaClass.simpleName
    Log.e(tag, "$className: $str")
    if (BuildConfig.ENABLE_FILE_LOG || ALWAYS_FILE_LOG) {
        FileLog.write("E $className: $tag $str", true)
    }
}

inline fun <THIS : Any> THIS.loge(tag:String = TAG, exception: Throwable, crossinline block: (THIS) -> String) {
    val str = block(this)
    val className = this.javaClass.simpleName
    val sb = StringBuilder()
    sb.append(exception.message).append("\n").append(exception.cause).append("\n")
    for (element in exception.stackTrace) {
        sb.append(element.toString()).append(System.lineSeparator())
    }
    sb.toString()
    Log.e(tag, "$className: $str")
    Log.e(tag, "$className: $sb")
    if (BuildConfig.ENABLE_FILE_LOG || ALWAYS_FILE_LOG) {
        FileLog.write("E $className: $str\nE $className: $tag: $sb", true)
    }
}

inline fun <THIS : Any> THIS.logd(crossinline block: (THIS) -> String) {
    val str = block(this)
    val className = this.javaClass.simpleName
    if (BuildConfig.DEBUG || ALWAYS_LOG) {
        Log.d(TAG, "$className: $str")
    }
    if (BuildConfig.ENABLE_FILE_LOG || ALWAYS_FILE_LOG) {
        FileLog.write("D $className: $str", false)
    }
}


inline fun <THIS : Any> THIS.logd(tag:String, crossinline block: (THIS) -> String) {
    val str = block(this)
    val className = this.javaClass.simpleName
    if (BuildConfig.DEBUG || ALWAYS_LOG) {
        Log.d(tag, "$className: $str")
    }
    if (BuildConfig.ENABLE_FILE_LOG || ALWAYS_FILE_LOG) {
        FileLog.write("D $className: $tag: $str", false)
    }
}

inline fun logdNoFile(tag:String = TAG, block:()->String) {
    if (BuildConfig.DEBUG || ALWAYS_LOG) {
        Log.d(tag, block())
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