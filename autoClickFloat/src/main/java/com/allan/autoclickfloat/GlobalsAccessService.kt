package com.allan.autoclickfloat

import android.accessibilityservice.AccessibilityService
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.MutableLiveData
import com.allan.autoclickfloat.activities.autooneclick.AutoContinuousClickActivityFragment
import com.allan.autoclickfloat.floats.FloatingManager
import com.allan.autoclickfloat.floats.bean.ACTION_STOP
import com.allan.autoclickfloat.floats.bean.AutoClickInfo
import com.au.module_android.Globals
import com.au.module_android.utils.ForeNotificationUtil

/**
 * @author allan
 * @date :2024/3/19 10:04
 * @description:
 */
class GlobalsAccessService : AccessibilityService() {
    companion object {
        const val TAG = "NongYaoAutoClickService"

        private var isAlive = false

        val isEnabledLiveData = MutableLiveData<Any>()
    }

    private var broadcastHandler: BroadcastHandler? = null

    override fun onCreate() {
        super.onCreate()
        isAlive = true
        Log.d("allan", "onCreate on start command====")
        ForeNotificationUtil.startForeground(
            this,
            "NongyaoAutoClickChannel",
            "NonNongyaoAutoClickChannelDesc",
            "AShoot悬浮点击",
            "AShoot正在工作中，点击关闭。",
            R.drawable.ic_nongyao_click,
            pendingIntent = PendingIntent.getActivity(this, 0, Intent(Globals.app, AllPermissionActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        )

        broadcastHandler = BroadcastHandler(this).also { it.register() }
        isEnabledLiveData.value = Unit
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
        isEnabledLiveData.value = Unit
        isAlive = false
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //当界面发生改变时，这个方法就会被调用，界面改变的具体信息就会包含在这个参数中。
//        performGlobalAction(GLOBAL_ACTION_DPAD_DOWN)
//        performGlobalAction(GLOBAL_ACTION_DPAD_UP)
//        Log.d("allan", "onAccessibilityEvent $event")
        // 当快捷方式开关变化时，此方法会被调用
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            // 检查事件内容，判断是否是快捷方式开关
            // 快捷方式开关通常是一个特定的窗口，你需要根据实际情况来判断
            if (event.className == GlobalsAccessService::class.java.name) {
                // 快捷方式开关状态发生了变化
                // 在这里处理你的逻辑
            }
        }
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