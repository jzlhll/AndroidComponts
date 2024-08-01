package com.allan.autoclickfloat.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import com.allan.autoclickfloat.AllPermissionActivity
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.activities.autooneclick.AutoContinuousClickObserver
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.nongyao.AppClickTasksObserver
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.utils.ForeNotificationUtil

/**
 * @author allan
 * @date :2024/3/19 10:04
 * @description:
 */
class AutoClickFloatAccessService : AccessibilityService() {
    companion object {
        val isEnabledLiveData = NoStickLiveData<Boolean>()
    }

    private val children:List<AbsAccessServiceObserver> = listOf(
        AutoContinuousClickObserver(this),
        AppClickTasksObserver(this)
    )
    private var broadcastHandler: BroadcastHandler? = null

    override fun onCreate() {
        super.onCreate()
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

        children.forEach {
            it.onCreate()
        }

        isEnabledLiveData.setValueSafe(true)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val config = AccessibilityServiceInfo()
        //配置监听的事件类型为界面变化|点击事件
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or AccessibilityEvent.TYPE_VIEW_CLICKED
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS
        setServiceInfo(config)
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

        children.forEach {
            it.onDestroy()
        }

        isEnabledLiveData.setValueSafe(false)

        broadcastHandler?.unregister()
        Log.d(Const.TAG, "service onDestroy")
        ForeNotificationUtil.stopForeground(this)
    }

    fun tryGetActivity(componentName:ComponentName) :ActivityInfo? {
        return try {
            packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        children.forEach {
            it.onAccessibilityEvent(event)
        }
    }

    /**
     * 获得当前视图根节点
     * */
    fun getCurrentRootNode() = try {
        rootInActiveWindow
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }

    override fun onInterrupt() { //辅助服务被中断了
    }

    private fun onScreenOff() {
        children.forEach {
            it.onScreenOff()
        }
    }

    private fun onScreenOn() {
        children.forEach {
            it.onScreenOn()
        }
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
                        onScreenOff()
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        onScreenOn()
                    }
                }
            }
        }
    }
}
