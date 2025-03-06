package com.allan.autoclickfloat.activities.autofs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.widget.Toast
import com.au.module_android.utils.logd

class AlarmReceiver : BroadcastReceiver() {
    private fun doIt(context: Context) {
        val pm = context.packageManager
        val intent = pm.getLaunchIntentForPackage("com.ss.android.lark")
        if (intent != null) {
            Toast.makeText(context, "launch apk ...", Toast.LENGTH_SHORT).show()
            /////context.startOutActivity(intent)
        } else {
            Toast.makeText(context, "No target apk!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // 1. 获取WakeLock保持设备唤醒
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::AlarmWakeLock"
        )
        wakeLock.acquire(10 * 1000) // 30秒超时防止无限占用
        try {
            // 2. 执行定时任务（例如启动服务、发送通知等）
            logd { "allan-alarm do it in onReceiver!!!" }
            doIt(context)
        } finally {
            wakeLock.release() //try不做释放
        }
    }
}