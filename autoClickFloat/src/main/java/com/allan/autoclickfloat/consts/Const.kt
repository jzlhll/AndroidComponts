package com.allan.autoclickfloat.consts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import com.allan.autoclickfloat.floats.WindowMgr
import com.au.module_android.Globals
import com.au.module_android.simplelivedata.NoStickLiveData

object Const {
    const val TAG = "autoclickfloat"

    val autoOnePoint = OnePointAuto()

    val appClickTasks = AppClickTasks()

    val rotationLiveData = NoStickLiveData(WindowMgr.mWindowManager.defaultDisplay.rotation)

    private val mConfigChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val oration = WindowMgr.mWindowManager.defaultDisplay.rotation
            Log.d(TAG, "onSreenOrientationConfigChange getRotation = $oration")
            rotationLiveData.setValueSafe(oration)
        }
    }

    init {
        Log.d(TAG, "Const init....")
        autoOnePoint.loadAutoOnePoint()
        autoOnePoint.loadAutoOnePointMs()

        val configChangeFilter = IntentFilter()
        configChangeFilter.addAction(Intent.ACTION_CONFIGURATION_CHANGED)
        Globals.app.registerReceiver(mConfigChangeReceiver, configChangeFilter)
    }
}