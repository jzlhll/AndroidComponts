package com.au.module_android.ui

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.au.module_android.R
import com.au.module_android.crash.UncaughtExceptionHandlerObj
import com.au.module_android.utils.FileLog
import com.au.module_android.utils.getAppIntent
import com.au.module_android.utils.startActivityFix

/**
 * @author allan
 * @date :2024/9/19 14:47
 * @description:
 */
class CrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        findViewById<View>(R.id.btReStart).setOnClickListener {
            getAppIntent(this@CrashActivity, this@CrashActivity.packageName)?.component?.className?.let {
                finish()
                startActivityFix(Intent(this.applicationContext, Class.forName(it)))
            }
        }
    }
}