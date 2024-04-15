package com.allan.nongyaofloat.floats

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import com.allan.nongyaofloat.floats.bean.ACTION_CLOSE
import com.allan.nongyaofloat.floats.bean.AutoClickInfo
import com.au.module_android.utils.ForeNotificationUtil

/**
 * @author allan
 * @date :2024/3/19 10:04
 * @description:
 */
class NongYaoAutoClickService : AccessibilityService() {
    companion object {
        const val TAG = "NongYaoAutoClickService"
    }

    private var broadcastHandler:BroadcastHandler? = null

    override fun onCreate() {
        super.onCreate()
        ForeNotificationUtil.startForeground(
            this,
            "Nongyao Auto click channelName",
            "Nongyao channel desc",
            "nongyao content title",
            "nongyao content text"
        )

        broadcastHandler = BroadcastHandler(this).also { it.register() }
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastHandler?.unregister()
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