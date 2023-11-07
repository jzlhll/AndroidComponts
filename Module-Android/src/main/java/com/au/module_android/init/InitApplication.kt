package com.au.module_android.init

import android.app.Application
import android.content.Context

/**
 * @author allan.jiang
 * @date :2023/11/7 14:32
 * @description:
 */
open class InitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirstInitial().init(this)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }
}