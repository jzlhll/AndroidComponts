package com.allan.autoclickfloat.floats

import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.Observer
import com.allan.autoclickfloat.AutoClickActivity
import com.allan.autoclickfloat.floats.bean.ACTION_CLOSE
import com.allan.autoclickfloat.floats.bean.AutoClickInfo
import com.au.module_android.Globals
import com.au.module_android.utils.ForeNotificationUtil

/**
 * @author allan
 * @date :2024/3/19 10:04
 * @description:
 */
class AutoClickService : AccessibilityService() {
    companion object {
        const val TAG = "NongYaoAutoClickService"
    }

    private var broadcastHandler:BroadcastHandler? = null

    override fun onCreate() {
        super.onCreate()
        ForeNotificationUtil.startForeground(
            this,
            "NongyaoAutoClickChannel",
            "NonNongyaoAutoClickChannelDesc",
            "悬浮点击",
            "正在工作中，点击关闭",
            pendingIntent = PendingIntent.getActivity(this, 0, Intent(Globals.app, AutoClickActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        )

        broadcastHandler = BroadcastHandler(this).also { it.register() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getStringExtra("action") == "stopService") {
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastHandler?.unregister()
        ForeNotificationUtil.stopForeground(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //当界面发生改变时，这个方法就会被调用，界面改变的具体信息就会包含在这个参数中。
//        performGlobalAction(GLOBAL_ACTION_DPAD_DOWN)
//        performGlobalAction(GLOBAL_ACTION_DPAD_UP)
    }

    override fun onInterrupt() { //辅助服务被中断了
    }

    private inner class BroadcastHandler(val context: Context) : BroadcastReceiver() {
        fun register() {
            context.registerReceiver(
                this,
                IntentFilter().apply {
                    //息屏关闭自动点击事件
                    addAction(Intent.ACTION_SCREEN_OFF)
                }
            )
        }

        fun unregister() {
            context.unregisterReceiver(this)
        }

        override fun onReceive(p0: Context?, intent: Intent?) {
            intent?.apply {
                when (action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        FloatingManager.autoClickLiveData.setValueSafe(AutoClickInfo(ACTION_CLOSE))
                    }
                }
            }
        }
    }
}