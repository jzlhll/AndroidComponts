package com.allan.androidlearning.transfer

import android.app.PendingIntent
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import com.allan.androidlearning.transfer.views.ShareImportActivity
import com.au.module_android.Globals
import com.au.module_android.Globals.resStr
import com.au.module_android.service.AutoStopService
import com.au.module_android.utils.logd

class MyDroidKeepLiveService : AutoStopService() {
    companion object {
        private var isRunning = false

        fun keepMyDroidAlive() {
            Globals.app.startForegroundService(Intent(Globals.app, MyDroidKeepLiveService::class.java).putExtra("myDroidAction", "start"))
        }

        fun stopMyDroidAlive() {
            if (isRunning) {
                Globals.app.startForegroundService(Intent(Globals.app, MyDroidKeepLiveService::class.java).putExtra("myDroidAction", "stop"))
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        isRunning = true
    }

    override fun foregroundType(): Int? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
        } else {
            null
        }
    }

    override fun getNotifyName(): String {
        return com.allan.androidlearning.R.string.mydroid_running_only_for_alive.resStr()
    }

    override fun getPendingIntent(): PendingIntent {
        val app = this.application
        val intent = Intent(app, ShareImportActivity::class.java)
        return PendingIntent.getActivity(app, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    override fun onHandleWork(intent: Intent, startIdStr: String) {
        val myDroidAction = intent.getStringExtra("myDroidAction")
        logd { "on handle work my Droid Action....$myDroidAction" }
        //start 不能stopWrap
        if (myDroidAction == "stop") {
            forceStop()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
    }

}