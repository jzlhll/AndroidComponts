package com.au.module_android.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.au.module_android.R
import com.au.module_android.click.onClick
import com.au.module_android.crash.UncaughtExceptionHandlerObj
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.getAppIntent
import com.au.module_android.utils.logd
import com.au.module_android.utils.openUrlByBrowser
import com.au.module_android.utils.startActivityFix

/**
 * @author allan
 * @date :2024/9/19 14:47
 * @description:
 */
class CrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logd { "onCreateeeeee" }
        setContentView(R.layout.activity_crash_layout)
        val errorInfo = Html.fromHtml(
            intent.getStringExtra(UncaughtExceptionHandlerObj.KEY_INFO)
        )
        val version = intent.getStringExtra(UncaughtExceptionHandlerObj.KEY_VERSION)
        val threadInfo = intent.getStringExtra(UncaughtExceptionHandlerObj.KEY_THREAD_INFO)
        findViewById<TextView>(R.id.versionName).text = "appVersion:" + version
        findViewById<TextView>(R.id.tvInfo).text = threadInfo + "\n" + errorInfo

        //格式化时间，作为Log文件名
        FileLog.write(version + "\n" + threadInfo + "\n" + errorInfo)

        findViewById<View>(R.id.restartBtn).setOnClickListener {
            android.os.Process.killProcess(android.os.Process.myPid())
            Runtime.getRuntime().exit(-1) //不能只依赖killProcess
            getAppIntent(this@CrashActivity, this@CrashActivity.packageName)?.component?.className?.let {
                finish()
                startActivityFix(Intent(this.applicationContext, Class.forName(it)))
            }
        }

        findViewById<View>(R.id.finishBtn).setOnClickListener {
            getAppIntent(this@CrashActivity, this@CrashActivity.packageName)?.component?.className?.let {
                finish()
                startActivityFix(Intent(this.applicationContext, Class.forName(it)))
            }
        }
        findViewById<TextView>(R.id.feedbackText).text = Html.fromHtml("可以尝试点击【忽略】或者【重启应用】按钮，如果反复出现，可以通过【检查并升级】更新app版本解决问题。如果仍然反复出现，请联系我们。<b>feedback</b>")
        findViewById<TextView>(R.id.feedbackText).onClick {
            openUrlByBrowser(Uri.parse("https://www.baidu.com"), this)
        }
    }
}