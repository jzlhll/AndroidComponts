package com.allan.autoclickfloat.activities.autofs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import com.au.module_android.Globals
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.logd
import com.au.module_android.utils.loge
import com.au.module_android.utils.startActivityFix

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        fun start(context: Context) {
            val l = context.packageManager.getLaunchIntentForPackage(context.packageName)!!

            val className = l.component?.className
            val found = Globals.activityList.find { className?.contains(it.javaClass.simpleName) == true}
            if (found == null) {
                context.startActivityFix(l.also {
                    it.putExtra("alarm", "alarmIsComingWhenNoStartActivity")
                })
            } else {
                FragmentShellActivity.start(context, AutoFsScreenOnFragment::class.java)
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent?) {
        // 1. 获取WakeLock保持设备唤醒
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::AlarmWakeLock"
        )

        val targetTsLong = intent?.getLongExtra(AutoFsObj.EXTRA_TARGET_TS_LONG, 0L)
        val targetTsInfo = intent?.getStringExtra(AutoFsObj.EXTRA_TARGET_TS_INFO)
        if (targetTsLong != null) {
            val curTs = System.currentTimeMillis()
            val deltaTs = curTs - targetTsLong
            if (deltaTs > 2 * 60 * 1000) { //比目标时间晚了2分钟才执行
                logd { "Alarm>>>> do it too late. $targetTsInfo" }
            } else if (deltaTs > -2 * 60 * 1000) { //比目标时间早了2分钟才执行~晚了2分钟区间内，都算作正常执行
                logd { "Alarm>>>> do it good. $targetTsInfo" }
            } else { //比目标时间早了2分钟才执行~晚了2分钟区间内，都算作正常执行
                logd { "Alarm>>>> do it too early. $targetTsInfo" }
            }
        } else {
            logd { "Alarm>>>> do it in intent no extra." }
        }

        wakeLock.acquire(30 * 1000)
        try {
            // 2. 执行定时任务（例如启动服务、发送通知等）
            start(context)
        } catch (e: Exception) {
          loge(exception = e) {"on receiver."}
        } finally {
            //wakeLock.release() //try不做释放
        }
    }
}
