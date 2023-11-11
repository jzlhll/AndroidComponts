package com.au.module_android.init

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.au.module_android.Globals
import com.au.module_android.hooks.optimizeSpTask
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.utils.ALog

/**
 * 自动初始化
 */
object FirstInitial {
    var isInitSharedPrefHook = false
    var isFontNoSpace = true

    fun init(context: Context): Application {
        val app = context as Application
        ALog.d("first init....")
        ToutiaoScreenAdapter.init(app)
        Globals.internalApp = app

        if (isInitSharedPrefHook) {
            optimizeSpTask()
        }

        return app
    }
}