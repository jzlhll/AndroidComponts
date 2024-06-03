package com.allan.autoclickfloat.activities.autooneclick

import android.util.Log
import androidx.lifecycle.Observer
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.accessibility.AutoClickFloatAccessService
import com.allan.autoclickfloat.accessibility.AbsAccessServiceObserver
import com.au.module_android.postToMainHandler
import com.au.module_android.removeFromMainHandler

class AutoContinuousClickObserver(service: AutoClickFloatAccessService) : AbsAccessServiceObserver(service) {
    private val onceRun = Runnable {
        val point = Const.autoOnePoint.autoOnePointLocLiveData.value
        Log.d(Const.TAG, "click point ${point?.first} ${point?.second}")
        tap(point?.first?.toFloat() ?: 100f, point?.second?.toFloat() ?: 200f)

        Const.autoOnePoint.autoOnePointBeClickedData.setValueSafe(Unit)
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

    override fun onScreenOff() {
        onDestroy()
    }

    override fun onScreenOn() {
    }
}