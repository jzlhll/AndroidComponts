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
            val pair = AutoFsObj.findLaunchActivity(context)
            if (!pair.second) {
                context.startActivityFix(pair.first.also {
                    it.putExtra("alarm", "alarmIsComingWhenNoStartActivity")
                })
            } else {
                FragmentShellActivity.start(context, AutoFsScreenOnFragment::class.java)
            }
        }
    }

    fun formatTimeDifference(curTs: Long, targetTsLong: Long): String {
        val deltaMs = curTs - targetTsLong
        val deltaMinutes = deltaMs / (60 * 1000)
        return when {
            deltaMinutes > 0 -> "已过去${deltaMinutes}分钟"
            deltaMinutes < 0 -> "提前${-deltaMinutes}分钟"
            else -> "准时"
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
            val info = formatTimeDifference(curTs, targetTsLong)
            if (deltaTs > 60 * 1000) {
                logd { "Alarm $targetTsInfo >>>> do it too late. $info" }
            } else if (deltaTs > -60 * 1000) {
                logd { "Alarm $targetTsInfo >>>> do it good. $info" }
            } else {
                logd { "Alarm $targetTsInfo >>>> do it too early. $info" }
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
