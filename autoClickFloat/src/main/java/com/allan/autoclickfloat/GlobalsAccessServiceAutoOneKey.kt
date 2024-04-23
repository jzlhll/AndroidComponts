package com.allan.autoclickfloat

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import androidx.lifecycle.Observer
import com.allan.autoclickfloat.consts.Const
import com.au.module_android.postToMainHandler
import com.au.module_android.removeFromMainHandler

class GlobalsAccessServiceAutoOneKey(service: GlobalsAccessService) : GlobalsAccessServiceObserver(service) {
    private val gestureResultCallback = object : AccessibilityService.GestureResultCallback() {
        override fun onCompleted(gestureDescription: GestureDescription?) {
            super.onCompleted(gestureDescription)
            Log.d(Const.TAG, "自动点击完成")
        }

        override fun onCancelled(gestureDescription: GestureDescription?) {
            super.onCancelled(gestureDescription)
            Log.d(Const.TAG, "自动点击取消")
        }
    }

    private val onceRun = Runnable {
        val point = Const.autoOnePoint.autoOnePointLocLiveData.value
        Log.d(Const.TAG, "click point ${point?.first} ${point?.second}")
        val path = Path()
        path.moveTo(point?.first?.toFloat() ?: 100f, point?.second?.toFloat() ?: 200f)
        val gestureDescription = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 100L, 100L))
            .build()
        val r = service.dispatchGesture(
            gestureDescription,
            gestureResultCallback,
            null
        )

        Const.autoOnePoint.autoOnePointBeClickedData.setValueSafe(Unit)

        Log.d(Const.TAG, "dispatch gesture $r")
        startAutoClickRun()
    }

    private fun startAutoClickRun() {
        postToMainHandler(onceRun, Const.autoOnePoint.autoOnePointClickMsLiveData.value?.toLong() ?: 250)
    }

    private fun stopAutoClickView() {
        Log.d(Const.TAG, "stopAutoClickView")
        removeFromMainHandler(onceRun)
    }

    private var openObserver = Observer<Boolean> {
        Log.d(Const.TAG, "open ob $it")
        if (it) {
            startAutoClickRun()
        } else {
            stopAutoClickView()
        }
    }

    override fun onCreate() {
        Log.d(Const.TAG, "onekey on create")
        Const.autoOnePoint.autoOnePointOpenLiveData.observeForever(openObserver)
    }

    override fun onDestroy() {
        Log.d(Const.TAG, "onekey on destroy")
        Const.autoOnePoint.autoOnePointOpenLiveData.removeObserver(openObserver)
        stopAutoClickView()
    }
}