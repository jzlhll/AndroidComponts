package com.allan.autoclickfloat.screen

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjection
import com.au.module_android.service.AutoStopService

class ScreenDetectService : AutoStopService() {
    companion object {
        fun start(context:Context, projection: MediaProjection?) {
            if (projection == null) {
                return
            }
            ScreenDetectConst.getInstance().projection = projection

            context.startService(Intent(context, ScreenDetectService::class.java).also {
                it.putExtra("screenDetect", "start")
            })
        }

        fun stop(context: Context) {
            context.startService(Intent(context, ScreenDetectService::class.java).also {
                it.putExtra("screenDetect", "stop")
            })
        }
    }

    override fun getNotifyName(): String {
        return "Screen detect"
    }

    override fun getPendingIntent(): PendingIntent? {
        return null
    }

    override fun onHandleWork(intent: Intent, startIdStr: String) {
        val action = intent.getStringExtra("screenDetect")
        when (action) {
            "start"-> {

            }
            "stop" -> {
                stopWrapSelf()
                ScreenDetectConst.getInstance().onDestroy()
            }
        }
    }
}