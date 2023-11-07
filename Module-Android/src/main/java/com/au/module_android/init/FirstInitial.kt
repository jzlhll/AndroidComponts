package com.au.module_android.init

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.au.module_android.Globals
import com.au.module_android.hooks.optimizeSpTask

/**
 * 自动初始化
 */
class FirstInitial {
    private val isInitSharedPrefHook = false

    fun init(context: Context): Application {
        val app = context as Application
        Globals.internalApp = app

        if (isInitSharedPrefHook) {
            optimizeSpTask()
        }

        return app
    }
}