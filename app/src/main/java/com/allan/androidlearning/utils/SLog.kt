package com.allan.androidlearning.utils

/**
 * @author allan
 * Date: 2023/1/11
 * Description TODO
 */

import android.os.Looper
import com.allan.androidlearning.BuildConfig

private const val TAG:String = "allan_app"
/**
 * E日志强制输出。并落盘。
 */
fun loge(s:String, tag:String="") {
    android.util.Log.e(TAG, "$tag: $s")
}

fun logw(s:String, tag:String="") {
    android.util.Log.w(TAG, "$tag: $s")
}

fun logd(s:String, tag:String="") {
    if (BuildConfig.DEBUG) {
        android.util.Log.d(TAG, "$tag: $s")
    }
}

fun logt(s:String, tag:String="") {
    if (BuildConfig.DEBUG) {
        android.util.Log.d(TAG, "$tag: thread${Thread.currentThread().id}: $s")
    }
}

fun logm(s:String, tag:String="") {
    if (BuildConfig.DEBUG) {
        val isMainThread = (Thread.currentThread().id == Looper.getMainLooper().thread.id)
        android.util.Log.d(TAG, "$tag: isMain($isMainThread): $s")
    }
}