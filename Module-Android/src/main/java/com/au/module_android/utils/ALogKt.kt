package com.au.module_android.utils

import android.os.Looper
import android.util.Log
import com.au.module.android.BuildConfig

const val TAG:String = "au_log"

inline fun loge(block:()->String) {
    android.util.Log.e(TAG, block())
}

inline fun logw(block:()->String) {
    android.util.Log.w(TAG, block())
}

inline fun logd(block:()->String) {
    if(BuildConfig.DEBUG) android.util.Log.d(TAG, block())
}

inline fun logt(block:()->String) {
    if(BuildConfig.DEBUG) {
        val isMainThread = (Thread.currentThread().id == Looper.getMainLooper().thread.id)
        android.util.Log.d(TAG, "thread${Thread.currentThread().id}-$isMainThread: " + block())
    }
}

fun logStace(s: String) {
    Log.d(TAG, "$s...start...")
    val ex = Exception()
    ex.printStackTrace()
    Log.d(TAG, "$s...end!")
}