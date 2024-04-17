package com.allan.autoclickfloat.floats

import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.allan.autoclickfloat.AutoClickActivity
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.floats.bean.ACTION_STOP
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

        fun Context.startAutoClickService() {
            if (!isAlive) {
                val intent = Intent(this, AutoClickService::class.java)
                startForegroundService(intent)
            }
        }

        fun Context.stopAutoClickService() {
            if (isAlive) {
                val intent = Intent(this, AutoClickService::class.java).also {
                    it.putExtra("myAction", "stopService")
                }
                startService(intent)
            }
        }

        private var isAlive = false
    }

    private var broadcastHandler:BroadcastHandler? = null

    override fun onCreate() {
        super.onCreate()
        isAlive = true
        Log.d("allan", "onCreate on start command====")
        ForeNotificationUtil.startForeground(
            this,
            "NongyaoAutoClickChannel",
            "NonNongyaoAutoClickChannelDesc",
            "悬浮点击",
            "正在工作中，点击关闭",
            R.drawable.ic_nongyao_click,
            pendingIntent = PendingIntent.getActivity(this, 0, Intent(Globals.app, AutoClickActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        )

        broadcastHandler = BroadcastHandler(this).also { it.register() }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("allan", "service on start command====")
        if (intent?.getStringExtra("myAction") == "stopService") {
            ForeNotificationUtil.stopForeground(this)
            Log.d("allan", "on start command stop service")
            stopSelf()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastHandler?.unregister()
        Log.d("allan", "service onDestroy")
        ForeNotificationUtil.stopForeground(this)
        isAlive = false
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
                        FloatingManager.autoClickLiveData.setValueSafe(AutoClickInfo(ACTION_STOP))
                    }
                }
            }
        }
    }
}