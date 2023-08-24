package com.au.module_android

import android.app.Activity
import android.app.Application
import android.os.Handler
import android.os.Looper
import com.au.module_android.utils.secondLastOrNull
import com.google.gson.Gson
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.MainScope


object Globals {
    /**
     * 全局协程作用域
     */
    val mainScope by lazy { MainScope() }

    /**
     * 主线程的Handler
     */
    val mainHandler by lazy { Handler(Looper.getMainLooper()) }

    /**
     * gson对象
     */
    val gson: Gson by lazy { Gson() }

    /**
     * 腾讯的数据存储库
     */
    val mmkv by lazy {
        MMKV.initialize(app)
        MMKV.defaultMMKV()
    }

    internal lateinit var internalApp: Application
    /**
     * 全局application
     */
    val app: Application
        get() = internalApp

    private val internalActivityList = ArrayList<Activity>(8)

    val activityList: ArrayList<Activity>
        get() = internalActivityList

    val topActivity:Activity?
        get() = activityList.lastOrNull()

    val secondTopActivity:Activity?
        get() = activityList.secondLastOrNull()
}