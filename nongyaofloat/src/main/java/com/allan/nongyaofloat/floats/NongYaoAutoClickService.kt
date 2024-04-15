package com.allan.nongyaofloat.floats

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.accessibility.AccessibilityEvent
import com.au.module_android.utils.ForeNotificationUtil

/**
 * @author allan
 * @date :2024/3/19 10:04
 * @description:
 */
class NongYaoAutoClickService : AccessibilityService() {
    companion object {
        //打开悬浮窗
        const val ACTION_SHOW = "action_show"

        //自动点击事件 开启/关闭
        const val ACTION_PLAY = "action_play"
        const val ACTION_STOP = "action_stop"

        //关闭悬浮窗
        const val ACTION_CLOSE = "action_close"

        const val TAG = "NongYaoAutoClickService"

        const val BROADCAST_ACTION_AUTO_CLICK = "BROADCAST_ACTION_AUTO_CLICK"
    }

    //点击间隔
    private var mInterval = -1L

    //点击坐标xy
    private var mPointX = -1f
    private var mPointY = -1f

    //悬浮窗视图
    private lateinit var mFloatingView: FloatingClickView

    private val broadcastReceiver = BroadcastHandler(this)

    override fun onCreate() {
        super.onCreate()
        ForeNotificationUtil.startForeground(
            this,
            "Nongyao Auto click channelName",
            "Nongyao channel desc",
            "nongyao content title",
            "nongyao content text"
        )
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
                    addAction(BROADCAST_ACTION_AUTO_CLICK)
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
                        mFloatingView.remove()
                        mainScope?.cancel()
                    }

                    BROADCAST_ACTION_AUTO_CLICK -> {
                        when (getStringExtra(BroadcastConstants.KEY_ACTION)) {
                            ACTION_SHOW -> {
                                mFloatingView.remove()
                                mainScope?.cancel()
                                mInterval = getLongExtra(BroadcastConstants.KEY_INTERVAL, 5000)
                                mFloatingView.show()
                            }

                            ACTION_PLAY -> {
                                mPointX = getFloatExtra(BroadcastConstants.KEY_POINT_X, 0f)
                                mPointY = getFloatExtra(BroadcastConstants.KEY_POINT_Y, 0f)
                                mainScope = MainScope()
                                autoClickView(mPointX, mPointY)
                            }

                            ACTION_STOP -> {
                                mainScope?.cancel()
                            }

                            ACTION_CLOSE -> {
                                mFloatingView.remove()
                                mainScope?.cancel()
                            }

                            else -> {
                                Log.e(TAG, "action error")
                            }
                        }
                    }
                }
            }
        }
    }
}