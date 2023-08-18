package com.au.module_android.init

import android.app.Application
import android.content.Context
import androidx.startup.Initializer
import com.au.module_android.Globals

/**
 * 自动初始化
 */
class FirstInitial : Initializer<Application> {
    override fun create(context: Context): Application {
        val app = context as Application
        Globals.internalApp = app
        return app
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }

}