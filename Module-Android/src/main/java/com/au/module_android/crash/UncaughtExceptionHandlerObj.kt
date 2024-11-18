package com.au.module_android.crash

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import com.au.module_android.Globals
import com.au.module_android.ui.CrashActivity
import com.au.module_android.utils.logt
import com.au.module_android.utils.startActivityFix
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Locale

object UncaughtExceptionHandlerObj : Thread.UncaughtExceptionHandler {
    override fun uncaughtException(t: Thread, e: Throwable) {
        logt { "crash!! ${e.message}" }
        Globals.activityList.forEach {
            it.finish()
        }
        start(Globals.app, t, e)
        //android.os.Process.killProcess(android.os.Process.myPid())
        //System.exit(-1) //一定要加。不然不能停下来。

        if (Looper.getMainLooper().thread == t) {
            Looper.loop()
        }
    }

    fun init() {
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandlerObj)
    }

    const val KEY_INFO = "errorInfo"
    const val KEY_VERSION = "version"
    const val KEY_THREAD_INFO = "threadInfo"

    fun start(context: Context, t: Thread, e: Throwable) {
        context.startActivityFix(Intent(context, CrashActivity::class.java).also {
            val version = Array(1) {""}
            it.putExtra(KEY_INFO, getErrorInfo(context, e, version))
            it.putExtra(KEY_VERSION, version[0])
            it.putExtra(KEY_THREAD_INFO, "threadId=${t.id}" + ", name=${t.name}" + ", isMainThread:" + (t.id == Looper.getMainLooper().thread.id))
        })
    }

    private fun getErrorInfo(context: Context, e: Throwable, version:Array<String>): String {
        //用于存储设备信息
        val mInfo: MutableMap<String, String> = HashMap()
        val pm: PackageManager = context.packageManager
        val info: PackageInfo = pm.getPackageInfo(context.packageName, PackageManager.GET_ACTIVITIES)
        // 获取版本信息
        val versionName =
            if (TextUtils.isEmpty(info.versionName)) "未设置版本名称" else info.versionName
        version[0] = versionName
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toString() + ""
        } else {
            info.versionCode.toString() + ""
        }
        mInfo["versionName"] = versionName
        mInfo["versionCode"] = versionCode
        mInfo["brand"] = Build.BRAND
        mInfo["product"] = Build.PRODUCT
        return getErrorStackTrace(mInfo, e)
    }

    /**时间戳转日期*/
    private fun longTimeToStr(time: Long?, pattern: String = "yyyy-MM-dd HH:mm"): String {
        if (time == null) {
            return ""
        }
        val format = SimpleDateFormat(pattern, Locale.getDefault())
        return format.format(time).toString()
    }

    private fun getErrorStackTrace(mInfo: MutableMap<String, String>, e: Throwable): String {
        val stringBuffer = StringBuffer()
        stringBuffer.append(
            "${
                longTimeToStr(
                    System.currentTimeMillis(),
                    "yyyy-MM-dd HH:mm:ss"
                )
            }<br><br>"
        )
        stringBuffer.append("------------Stack---------<br>")
        val stringWriter = StringWriter()
        val writer = PrintWriter(stringWriter)
        e.printStackTrace(writer)
        var cause = e.cause
        while (cause != null) {
            cause.printStackTrace(writer)
            val nextCause = e.cause
            cause = if (nextCause != cause) {
                nextCause
            } else {
                null
            }
        }
        writer.close()
        val string: String = stringWriter.toString()
        stringBuffer.append(string)
        stringBuffer.append("<br><br>------------DeviceInfo---------<br>")
        for ((keyName, value) in mInfo) {
            stringBuffer.append("<b>$keyName：</b>$value<br>")
        }
        return stringBuffer.toString()
    }
}