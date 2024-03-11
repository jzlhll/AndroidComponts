package com.au.module_android.init

import android.app.Application
import android.content.Context
import androidx.startup.Initializer

/**
 * @author au
 * Date: 2023/7/20
 * Description 初始化，所有的初始化类，都继承它。
 * 这样好必须比FirstInitial晚。
 */
abstract class AbstractInitializer : Initializer<Application> {

    final override fun create(context: Context): Application {
        val app = context as Application
        onCreate(app)
        return app
    }

    abstract fun onCreate(app:Application)

    final override fun dependencies(): MutableList<Class<out Initializer<*>>> {
        return subDependencies() ?: mutableListOf()
    }

    open fun subDependencies(): MutableList<Class<out Initializer<*>>>? {return null}
}