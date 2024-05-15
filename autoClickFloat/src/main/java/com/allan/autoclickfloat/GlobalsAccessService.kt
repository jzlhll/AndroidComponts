package com.allan.autoclickfloat

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityService.GestureResultCallback
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.accessibilityservice.GestureDescription.StrokeDescription
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.lifecycle.MutableLiveData
import com.allan.autoclickfloat.consts.Const
import com.au.module_android.Apps
import com.au.module_android.utils.ForeNotificationUtil

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
            pendingIntent = PendingIntent.getActivity(this, 0, Intent(Apps.app, AllPermissionActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
        )

        broadcastHandler = BroadcastHandler(this).also { it.register() }
        isEnabledLiveData.value = Unit

        children.forEach {
            it.onCreate()
        }
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
        broadcastHandler?.unregister()
        Log.d(Const.TAG, "service onDestroy")
        ForeNotificationUtil.stopForeground(this)
        isEnabledLiveData.value = Unit
        isAlive = false
        children.forEach {
            it.onDestroy()
        }
    }

    private fun tryGetActivity(componentName:ComponentName) :ActivityInfo? {
        return try {
            packageManager.getActivityInfo(componentName, 0)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        //当界面发生改变时，这个方法就会被调用，界面改变的具体信息就会包含在这个参数中。
//        performGlobalAction(GLOBAL_ACTION_DPAD_DOWN)
//        performGlobalAction(GLOBAL_ACTION_DPAD_UP)
//        Log.d(Const.TAG, "onAccessibilityEvent $event")
        val nodeInfo = event?.source //当前界面的可访问节点信息
        if (event?.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {//界面变化事件
            val componentName = ComponentName(event.packageName.toString(), event.className.toString());
            val activityInfo = tryGetActivity(componentName)
            val isActivity = activityInfo != null

            if (isActivity) {
                Log.d("WindowChange", "allan 当前运行包名" + nodeInfo?.packageName)
                switch (nodeInfo.getPackageName().toString()) {
                    case “com.netease.cloudmusic”:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            skip(nodeInfo.findAccessibilityNodeInfosByViewId(“com.netease.cloudmusic:id/c3l”));
                        }
                    }, 500);
                    break;
                    case “cn.xiaochuankeji.zuiyouLite”:
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            skip(nodeInfo.findAccessibilityNodeInfosByViewId(“cn.xiaochuankeji.zuiyouLite:id/btn_skip”));
                        }
                    }, 2000);
                    break;

                    default: {
                    List nodeInfoList = nodeInfo.findAccessibilityNodeInfosByText(“跳过”);
                    for (AccessibilityNodeInfo info : nodeInfoList) {
                    CharSequence charSequence = info.getText();
                    if (charSequence != null) {
                        String msg = charSequence.toString();
                        if (msg.contains(“跳过”)) {
                            info.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                            Toast.makeText(this, “跳过广告”, Toast.LENGTH_SHORT).show();
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

    val gestureResultCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            Log.d(Const.TAG, "tap 自动点击完成")
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            Log.d(Const.TAG, "tap 自动点击取消")
        }
    }

    val swipeGestureResultCallback = object : GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            Log.d(Const.TAG, "swipe 自动点击完成")
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            Log.d(Const.TAG, "swipe 自动点击取消")
        }
    }

    open fun tap(x:Float, y:Float) {
        val path = Path()
        path.moveTo(x, y)
        val gestureDescription = GestureDescription.Builder()
            .addStroke(StrokeDescription(path, 0L, 200L)) //默认比较好的150ms。
            .build()
        val r = service.dispatchGesture(
            gestureDescription,
            gestureResultCallback,
            null
        )
        Log.d(Const.TAG, "tap dispatch gesture $r")
    }

    /**
     * 模拟滑动事件
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param startTime 0即可执行
     * @param duration  滑动时长
     * @return 执行是否成功
     */
    private fun swipe(x1: Int, y1: Int, x2: Int, y2: Int) {
        Log.e("Tag", "模拟滑动事件")

        val builder = GestureDescription.Builder()
        val p = Path()
        p.moveTo(x1.toFloat(), y1.toFloat())
        p.lineTo(x2.toFloat(), y2.toFloat())
        builder.addStroke(StrokeDescription(p, 0L, 400L)) //尝试修改时间。
        val gesture = builder.build()
        val r = service.dispatchGesture(gesture, swipeGestureResultCallback, null)
        Log.d(Const.TAG, "swipe dispatch gesture $r")
    }

}