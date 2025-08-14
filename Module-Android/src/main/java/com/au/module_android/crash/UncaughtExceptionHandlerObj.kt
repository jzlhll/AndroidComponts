package com.au.module_android.crash

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.text.TextUtils
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import com.au.module_android.Globals
import com.au.module_android.utils.getAppIntent
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logd
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.logt
import com.au.module_android.utils.startActivityFix
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Locale

object UncaughtExceptionHandlerObj : Thread.UncaughtExceptionHandler {
    const val TAG = "UncaughtExpHandObj"
    private var isInit = false

    private var enableEntryCreate = false

    /**
     * 外部可以设置的死在了Entry情况下，只能通过Toast给用户交互 runnable 类对象。
     */
    internal var entryCrashedRunnableClass:Class<out MaybeEntryCrashedRunnable>? = null
    fun setMaybeEntryCrashedRunnableClass(clazz:Class<out MaybeEntryCrashedRunnable>) {
        entryCrashedRunnableClass = clazz
    }

    private var manualLogUploader:((customLog:String, exception:Throwable)->Unit)? = null

    /**
     * 设置手动上传日志的接口。
     */
    fun setManualLogUploader(manualLogUploader:(customLog:String, exception:Throwable)->Unit) {
        this.manualLogUploader = manualLogUploader
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        manualUploadCrashLog("uncaughtException", e)
        crashAction(t, e, isThrowableMainThreadAndInOnCreate(Thread.currentThread(), e))
    }

    fun init(enableEntryCreate:Boolean = false) {
        if (isInit) return
        isInit = true

        UncaughtExceptionHandlerObj.enableEntryCreate = enableEntryCreate
        Thread.setDefaultUncaughtExceptionHandler(UncaughtExceptionHandlerObj)

        Handler(Looper.getMainLooper()).post {
            while (true) {
                //主线程异常拦截
                try {
                    Looper.loop()
                } catch (e: Throwable) {
                    logdNoFile(TAG) { "Crashed=======>>>" }
                    val isThrowableMainThreadAndInOnCreate = isThrowableMainThreadAndInOnCreate(Thread.currentThread(), e)
                    logd(TAG) { "uncaughtException2 loop crash: " + e.message + ", isCreateMain: " + isThrowableMainThreadAndInOnCreate }
                    e.printStackTrace()
                    //主线程Activity，Fragment的create函数崩溃，导致界面无法显示。这种情况其实是很少的。
                    manualUploadCrashLog("main loop", e)
                    ignoreError {
                        crashAction(Thread.currentThread(), e, isThrowableMainThreadAndInOnCreate)
                    }
                    logdNoFile(TAG) { "<<<=======" }
                }
            }
        }
    }

    private fun manualUploadCrashLog(customLog:String, ex:Throwable) {
        //Firebase.crashlytics.log(customLog)
        //Firebase.crashlytics.recordException(ex)
        manualLogUploader?.invoke(customLog, ex)
    }

    private fun crashAction(t: Thread, e: Throwable, isThrowableMainThreadAndInOnCreate:Boolean) {
        logd { "crash action $e" }
        val isEntryCreateCrash = if (isThrowableMainThreadAndInOnCreate) {
            val startActivityName = getAppIntent(Globals.app, Globals.app.packageName)?.component?.className
            logt(TAG) { "crashed in an activity create: ${e.message}"}
            if (startActivityName != null) {
                e.message?.contains(startActivityName) == true
            } else {
                false
            }
        } else {
            false
        }

        if (isEntryCreateCrash) {
            logt(TAG) { "crashed in entry activity."}
            if(enableEntryCreate)
                MaybeEntryCrashedRunnable.create()
            else
                Toast.makeText(Globals.app, "Error! You app crash in entry create step", Toast.LENGTH_LONG).show()
            //对于启动activity的创建过程中crash，会出现白屏的可能性。
        } else {
            //非启动activity则没事，finish即可。 其中：create过程报错，不能finish之前的Activity。
            Globals.activityList.forEach {
                if(it is FragmentActivity && it.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    it.finish()
                }
            }
        }

        logt(TAG) { "startCrashActivity crash activity..." }
        startCrashActivity(Globals.app, t, e)
    }

    const val KEY_INFO = "errorInfo"
    const val KEY_VERSION = "version"
    const val KEY_THREAD_INFO = "threadInfo"

    private fun startCrashActivity(context: Context, t: Thread, e: Throwable) {
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
        version[0] = versionName ?: ""
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.longVersionCode.toString() + ""
        } else {
            info.versionCode.toString() + ""
        }
        mInfo["versionName"] = versionName ?: ""
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

    private fun isThrowableMainThreadAndInOnCreate(t:Thread, e:Throwable) : Boolean{
        if (t != Looper.getMainLooper().thread) {
            return false
        }

        if (e.message?.contains("Unable to start activity") == true) {
            return true
        }

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
        return string.contains("Activity.performStart")
                || string.contains("AppCompatActivity.onStart")
                || string.contains("Fragment.performCreate")
    }

    fun killAndRestart(activity: Activity?) {
        val ctx = activity ?: Globals.app
        getAppIntent(ctx, ctx.packageName)?.component?.className?.let {
            activity?.finish()
            ctx.startActivityFix(Intent(ctx.applicationContext, Class.forName(it)))
            Process.killProcess(Process.myPid())
            Runtime.getRuntime().exit(-1) //不能只依赖killProcess
        }
    }
}