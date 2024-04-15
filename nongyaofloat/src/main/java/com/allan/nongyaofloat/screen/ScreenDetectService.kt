package com.allan.nongyaofloat.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.util.Log
import com.au.module_android.service.AutoStopService
import java.nio.ByteBuffer

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