package com.allan.autoclickfloat

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.allan.autoclickfloat.activities.autofs.AutoFsObj
import com.au.module_android.utils.logd

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            logd { "allanAlarm receiver boot completed！" }
            // 从本地存储读取闹钟配置
            AutoFsObj.init()
            AutoFsObj.checkAndStartNextAlarm(context)
        }
    }
}