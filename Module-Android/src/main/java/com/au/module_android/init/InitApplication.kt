package com.au.module_android.init

import android.app.Application
import android.content.Context
import android.util.Log
import com.au.module_android.utils.ALog

/**
 * @author au
 * @date :2023/11/7 14:32
 * @description:
 */
open class InitApplication : Application() {
    override fun onCreate() {
        ALog.d("init application")
        FirstInitial.init(this)
        super.onCreate()
    }
}