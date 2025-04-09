package com.allan.autoclickfloat.activities.autofs

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.PowerManager
import com.au.module_android.Globals
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.utils.logd
import com.au.module_android.utils.startActivityFix

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        fun start(context: Context, autoFsId:String?) {
            val l = context.packageManager.getLaunchIntentForPackage(context.packageName)!!

            val className = l.component?.className
            val found = Globals.activityList.find { className?.contains(it.javaClass.simpleName) == true}
            if (found == null) {
                context.startActivityFix(l.also {
                    it.putExtra("alarm", "alarmIsComingWhenNoStartActivity")
                    if (autoFsId != null) {
                        it.putExtra("autoFsId", autoFsId)
                    }
                })
            } else {
                FragmentShellActivity.start(context, AutoFsScreenOnFragment::class.java, Bundle().apply {
                    if(autoFsId != null) putString("autoFsId", autoFsId)
                })
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
        val autoFsId = intent?.getStringExtra("autoFsId")
        wakeLock.acquire(30 * 1000)
        try {
            // 2. 执行定时任务（例如启动服务、发送通知等）
            logd { "allan-alarm do it in onReceiver!!!" }
            start(context, autoFsId)
        } finally {
            //wakeLock.release() //try不做释放
        }
    }
}
