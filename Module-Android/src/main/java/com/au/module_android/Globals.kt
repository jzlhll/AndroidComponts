package com.au.module_android

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.au.module_android.utils.secondLastOrNull
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.MainScope

object Globals {
    /**
     * 点击事件间隔时间
     */
    var globalPaddingClickTime = 180L

    /**
     * 全局协程作用域
     */
    val mainScope by lazy { MainScope() }

    /**
     * 主线程的Handler
     */
    val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    val backgroundHandler by lazy { createBackgroundHandler() }

    /**
     * gson对象
     */
    val gson: Gson by lazy { Gson() }

    /**
     * 全局application
     */
    val app: Application get() = internalApp

    val activityList: ArrayList<Activity>
        get() = internalActivityList

    val topActivity:Activity?
        get() = activityList.lastOrNull()

    val secondTopActivity:Activity?
        get() = activityList.secondLastOrNull()

    /**
     * 腾讯的数据存储库
     */
    val mmkv by lazy { MMKV.initialize(app);MMKV.defaultMMKV() }

    //内部参数
    lateinit var internalApp: Application
    internal val internalActivityList = ArrayList<Activity>(8)

    private fun createBackgroundHandler() : Handler {
        val handlerThread = HandlerThread("app-major-bg-thread")
        handlerThread.start()
        return Handler(handlerThread.looper)
    }
}

//----------------------handler start
fun postToMainHandler(run:Runnable) {
    Globals.mainHandler.post(run)
}

fun postToMainHandler(run:Runnable, delay:Long) {
    Globals.mainHandler.postDelayed(run, delay)
}

fun removeFromMainHandler(run:Runnable) {
    Globals.mainHandler.removeCallbacks(run)
}

fun postToBgHandler(run:Runnable) {
    Globals.backgroundHandler.post(run)
}

fun postToBgHandler(run:Runnable, delay:Long) {
    Globals.backgroundHandler.postDelayed(run, delay)
}

fun removeFromBgHandler(run:Runnable) {
    Globals.backgroundHandler.removeCallbacks(run)
}
//----------------------handler end