package com.au.module_android.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.Process
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.startActivityFix
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Locale
import com.au.module_android.R;
import com.au.module_android.Globals
import kotlin.system.exitProcess

/**
 * @author allan
 * @date :2024/9/19 14:47
 * @description:
 */
class CrashActivity : AppCompatActivity() {
    companion object {
        fun initUncaughtExceptionHandler() {
            Thread.setDefaultUncaughtExceptionHandler { t, e ->
                start(Globals.app, t, e)
                Process.killProcess(Process.myPid())
                exitProcess(-1)
            }
        }

        private const val KEY_INFO = "errorInfo"
        private const val KEY_VERSION = "version"
        private const val KEY_THREAD_INFO = "threadInfo"

        fun start(context: Context, t: Thread, e: Throwable) {
            context.startActivityFix(Intent(context, CrashActivity::class.java).also {
                val version = Array(1) {""}
                it.putExtra(KEY_INFO, getErrorInfo(context, e, version))
                it.putExtra(KEY_VERSION, version[0])
                it.putExtra(KEY_THREAD_INFO, "threadId=${t.id}" + ", name=${t.name}" + ", isMain:" + (t.id == Looper.getMainLooper().thread.id))
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
        fun longTimeToStr(time: Long?, pattern: String = "yyyy-MM-dd HH:mm"): String {
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crash_layout)
        val errorInfo = Html.fromHtml(
            intent.getStringExtra(KEY_INFO)
        )
        val version = intent.getStringExtra(KEY_VERSION)
        val threadInfo = intent.getStringExtra(KEY_THREAD_INFO)
        findViewById<TextView>(R.id.versionName).text = version
        findViewById<TextView>(R.id.tvInfo).text = errorInfo

        //格式化时间，作为Log文件名
        FileLog.write(version + "\n" + threadInfo + "\n" + errorInfo)

        findViewById<View>(R.id.btReStart).setOnClickListener {
            intent?.component?.className?.let {
                startActivityFix(Intent(this, Class.forName(it)))
                finish()
            }
        }
    }
}