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
import com.allan.autoclickfloat.consts.Const
import com.au.module_android.Globals
import com.au.module_android.utils.ForeNotificationUtil
import kotlinx.coroutines.MainScope

/**
 * @author allan
 * @date :2024/3/19 10:04
 * @description:
 */
class GlobalsAccessService : AccessibilityService() {
    companion object {
        var isAlive = false
        val isEnabledLiveData = MutableLiveData<Any>()
    }

    private var broadcastHandler: BroadcastHandler? = null

    private val children = listOf(GlobalsAccessServiceAutoOneKey(this))

    override fun onCreate() {
        super.onCreate()
        isAlive = true
        Log.d(Const.TAG, "onCreate on start command====")
        ForeNotificationUtil.startForeground(
            this,
            "AutoClickChannel",
            "AutoClickChannelDesc",
            "AShoot悬浮点击",
            "AShoot正在工作中，点击关闭。",
            R.drawable.ic_nongyao_click,
            pendingIntent = PendingIntent.getActivity(this, 0, Intent(Globals.app, AllPermissionActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        )

        broadcastHandler = BroadcastHandler(this).also { it.register() }
        isEnabledLiveData.value = Unit

        children.forEach {
            it.onCreate()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(Const.TAG, "service on start command====")
        if (intent?.getStringExtra("myAction") == "stopService") {
            ForeNotificationUtil.stopForeground(this)
            Log.d(Const.TAG, "on start command stop service")
            stopSelf()
        }
        children.forEach {
            it.onStartCommand(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastHandler?.unregister()
        Log.d(Const.TAG, "service onDestroy")
        ForeNotificationUtil.stopForeground(this)
        isEnabledLiveData.value = Unit
        isAlive = false
        children.forEach {
            it.onDestroy()
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //当界面发生改变时，这个方法就会被调用，界面改变的具体信息就会包含在这个参数中。
//        performGlobalAction(GLOBAL_ACTION_DPAD_DOWN)
//        performGlobalAction(GLOBAL_ACTION_DPAD_UP)
//        Log.d(Const.TAG, "onAccessibilityEvent $event")
        // 当快捷方式开关变化时，此方法会被调用
        if (event?.eventType == AccessibilityEvent.TYPE_VIEW_CLICKED) {
            Log.d(Const.TAG, "view click")
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
                    addAction(Intent.ACTION_SCREEN_ON)
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
                        children.forEach {
                            it.onDestroy()
                        }
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        children.forEach {
                            it.onCreate()
                        }
                    }
                }
            }
        }
    }

}

abstract class GlobalsAccessServiceObserver(val service: GlobalsAccessService) {
    abstract fun onCreate()

    abstract fun onDestroy()

    open fun onStartCommand(intent:Intent?) {}
}