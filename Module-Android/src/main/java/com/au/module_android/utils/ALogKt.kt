package com.au.module_android.utils

import android.util.Log
import com.au.module_android.BuildConfig

const val TAG:String = "au"
var ALWAYS_FILE_LOG = BuildConfig.ENABLE_FILE_LOG_DEFAULT

inline fun <THIS : Any> THIS.loge(tag:String, crossinline block: (THIS) -> String) {
    val str = block(this)
    val log = ALogJ.log("E", str, tag, this.javaClass)
    Log.e(TAG, log)

    if (ALWAYS_FILE_LOG) FileLog.write(log)
}

inline fun <THIS : Any> THIS.loge(crossinline block: (THIS) -> String) {
    val str = block(this)
    val log = ALogJ.log("E", str, this.javaClass)
    Log.e(TAG, log)

    if (ALWAYS_FILE_LOG) FileLog.write(log)
}

inline fun <THIS : Any> THIS.logw(tag:String, crossinline block: (THIS) -> String) {
    val str = block(this)
    val log = ALogJ.log("W", str, tag, this.javaClass)
    Log.w(TAG, log)

    if (ALWAYS_FILE_LOG) FileLog.write(log)
}

inline fun <THIS : Any> THIS.logw(crossinline block: (THIS) -> String) {
    val str = block(this)
    val log = ALogJ.log("W", str, this.javaClass)
    Log.w(TAG, log)

    if (ALWAYS_FILE_LOG) FileLog.write(log)
}

inline fun <THIS : Any> THIS.loge(tag:String, exception: Throwable, crossinline block: (THIS) -> String) {
    val str = block(this)
    val log = ALogJ.log("E", str, tag, this.javaClass)
    val ex = ALogJ.ex(exception)

    Log.e(TAG, log)
    Log.e(TAG, ex)
    if (ALWAYS_FILE_LOG) FileLog.write(log + "\n" + ex)
}

inline fun <THIS : Any> THIS.loge(exception: Throwable, crossinline block: (THIS) -> String) {
    val str = block(this)
    val log = ALogJ.log("E", str, this.javaClass)
    val ex = ALogJ.ex(exception)

    Log.e(TAG, log)
    Log.e(TAG, ex)
    if (ALWAYS_FILE_LOG) FileLog.write(log + "\n" + ex)
}

inline fun <THIS : Any> THIS.logd(crossinline block: (THIS) -> String) {
    if (BuildConfig.ENABLE_LOGCAT || ALWAYS_FILE_LOG) {
        val str = block(this)
        val log = ALogJ.log("D", str, this.javaClass)
        if(BuildConfig.ENABLE_LOGCAT) Log.d(TAG, log)

        if (ALWAYS_FILE_LOG) FileLog.write(log)
    }
}

inline fun <THIS : Any> THIS.logd(tag:String, crossinline block: (THIS) -> String) {
    if (BuildConfig.ENABLE_LOGCAT || ALWAYS_FILE_LOG) {
        val str = block(this)
        val log = ALogJ.log("D", str, tag, this.javaClass)
        if(BuildConfig.ENABLE_LOGCAT) Log.d(TAG, log)

        if (ALWAYS_FILE_LOG) FileLog.write(log)
    }
}

inline fun <THIS : Any> THIS.logdNoFile(crossinline block: (THIS) -> String) {
    if (BuildConfig.ENABLE_LOGCAT) {
        val str = block(this)
        val log = ALogJ.log("D", str, this.javaClass)
        Log.d(TAG, log)
    }
}

inline fun <THIS : Any> THIS.logdNoFile(tag:String = TAG, crossinline block: (THIS) -> String) {
    if (BuildConfig.ENABLE_LOGCAT) {
        val str = block(this)
        val log = ALogJ.log("D", str, tag, this.javaClass)
        Log.d(TAG, log)
    }
}

inline fun <THIS : Any> THIS.logt(tag:String = TAG, crossinline block: (THIS) -> String) {
    if (BuildConfig.ENABLE_LOGCAT) {
        val str = block(this)
        val log = ALogJ.logThread(str, this.javaClass)
        Log.d(tag, log)
    }
}

fun logStace(tag:String = TAG, s: String) {
    Log.d(tag, "$s...start...")
    val ex = Exception()
    ex.printStackTrace()
    Log.d(tag, "$s...end!")
}