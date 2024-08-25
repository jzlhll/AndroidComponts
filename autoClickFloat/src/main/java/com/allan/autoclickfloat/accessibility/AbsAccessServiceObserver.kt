package com.allan.autoclickfloat.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Intent
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import androidx.annotation.EmptySuper
import com.allan.autoclickfloat.consts.Const

abstract class AbsAccessServiceObserver(val service: AutoClickFloatAccessService) {
    abstract fun onCreate()

    abstract fun onDestroy()

    abstract fun onScreenOff()

    abstract fun onScreenOn()

    @EmptySuper
    open fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    open fun onStartCommand(intent: Intent?) {}

    val gestureResultCallback = object : AccessibilityService.GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            Log.d(Const.TAG, "tap 自动点击完成")
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            Log.d(Const.TAG, "tap 自动点击取消")
        }
    }

    val swipeGestureResultCallback = object : AccessibilityService.GestureResultCallback() {
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
            .addStroke(GestureDescription.StrokeDescription(path, 0L, 200L)) //默认比较好的150ms。
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
        builder.addStroke(GestureDescription.StrokeDescription(p, 0L, 400L)) //尝试修改时间。
        val gesture = builder.build()
        val r = service.dispatchGesture(gesture, swipeGestureResultCallback, null)
        Log.d(Const.TAG, "swipe dispatch gesture $r")
    }

}