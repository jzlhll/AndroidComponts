package com.au.module_android

import android.app.Activity
import android.app.Application
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.au.module_android.okhttp.AbsCookieJar
import com.au.module_android.okhttp.OkhttpClients
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.secondLastOrNull
import com.au.module_android.utils.unsafeLazy
import com.google.gson.Gson
import kotlinx.coroutines.MainScope
import okhttp3.OkHttpClient
import java.io.File

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
     * 选择合适的cacheDir
     */
    val goodCacheDir : File by unsafeLazy { app.externalCacheDir ?: app.cacheDir }

    /**
     * 选择合适的filesDir
     */
    val goodFilesDir : File by unsafeLazy { app.getExternalFilesDir(null) ?: app.filesDir }

    /**
     * 全局application
     */
    val app: Application get() = internalApp

    /**
     * application初始化完成的通知。时机就是我们把基础的app全局给设置好。避免有的地方无法调用到。
     */
    val firstInitialOnCreateData = NoStickLiveData<Any>()

    val activityList: ArrayList<Activity>
        get() = internalActivityList

    val topActivity:Activity?
        get() = activityList.lastOrNull()

    val secondTopActivity:Activity?
        get() = activityList.secondLastOrNull()

    //内部参数
    lateinit var internalApp: Application
    internal val internalActivityList = ArrayList<Activity>(8)

    private fun createBackgroundHandler() : Handler {
        val handlerThread = HandlerThread("app-major-bg-thread")
        handlerThread.start()
        return Handler(handlerThread.looper)
    }

    /**
     * 兼容dark模式获取资源
     * Application不具备识别night资源的能力。
     * 需要使用构建的themedContext。
     */
    @ColorInt
    fun getColor(@ColorRes resId:Int) : Int {
        return ContextCompat.getColor(app, resId)
    }

    /**
     * 兼容dark模式获取资源
     * Application不具备识别night资源的能力。
     * 需要使用构建的themedContext。
     */
    fun getDrawable(@ColorRes resId:Int) : Drawable? {
        return ContextCompat.getDrawable(app, resId)
    }

    /**
     * 兼容dark模式获取资源
     * Application不具备识别night资源的能力。
     * 需要使用构建的themedContext。
     */
    fun getString(@StringRes resId:Int) : String {
        return ContextCompat.getString(app, resId)
    }

    ///////////////okhttp
    private val okhttpClients by lazy { OkhttpClients() }
    /**
     * 是否信任所有证书,默认不信任，为了传输安全
     * okhttp（必须在创建之前使用）
     */
    var okHttpEnableTrustAllCertificates = false

    /**
     * 自定义cookie管理
     * okhttp（必须在创建之前使用）
     */
    var okHttpCookieJar: AbsCookieJar? = null

    /**
     * 设置okhttp缓存，如果大于0，开启缓存
     */
    var okHttpCacheSize = 0L

    /**
     * 额外的okhttp builder设置
     */
    var okhttpExtraBuilder : ((OkHttpClient.Builder)->Unit)? = null

    /**
     * 获取okhttpClient对象
     * @param timeOutMode 0 使用常规15秒的。默认。
     *                    1 使用快速5秒的quick版。
     *                    2 使用45秒的mid版。
     *                    3 使用5分钟的long版。
     */
    fun okHttpClient(timeOutMode: Int = 0) = when (timeOutMode) {
        1 -> okhttpClients.quickOkHttpClient
        2 -> okhttpClients.midTimeoutOkHttpClient()
        3 -> okhttpClients.longTimeoutOkHttpClient()
        else -> okhttpClients.okHttpClient
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