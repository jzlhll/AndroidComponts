package com.allan.nongyaofloat.screen

import android.content.Intent
import com.au.module_android.service.AutoStopService

class ScreenDetectService : AutoStopService() {


    override fun getNotifyName(): String {
        return "Screen detect"
    }

    override fun onHandleWork(intent: Intent, startIdStr: String) {
        val action = intent.getStringExtra("screenDetect")
        when (action) {
            "start"-> {

            }
            "stop" -> {

            }
        }
    }
}