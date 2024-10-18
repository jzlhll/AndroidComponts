package com.au.module_android

import android.app.Application
import androidx.lifecycle.ProcessLifecycleOwner
import com.au.module_android.init.GlobalActivityCallback
import com.au.module_android.init.GlobalBackgroundCallback
import com.au.module_android.init.optimizeSpTask
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.ui.UncaughtExceptionHandlerObj

/**
 * 自动初始化
 */
class FirstInitial {
    data class FirstInitialConfig(
        val isInitSharedPrefHook:Boolean = false,
        val isInitDarkMode:Boolean = true,
        val isEnableToutiaoScreenAdapter:Boolean = false,
        )

    fun init(context: Application, initCfg:FirstInitialConfig? = null): Application {
        Globals.internalApp = context

        UncaughtExceptionHandlerObj.init()

        val initConfig = initCfg ?: FirstInitialConfig()
        if(initConfig.isEnableToutiaoScreenAdapter) { ToutiaoScreenAdapter.init(context) }
        if (initConfig.isInitSharedPrefHook) { optimizeSpTask() }

        context.registerActivityLifecycleCallbacks(GlobalActivityCallback())
        ProcessLifecycleOwner.get().lifecycle.addObserver(GlobalBackgroundCallback)

        Globals.firstInitialOnCreateData.setValueSafe(Unit)
        return context
    }
}