package com.au.module_android.init

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ProcessLifecycleOwner
import com.au.module_android.Globals
import com.au.module_android.screenadapter.ToutiaoScreenAdapter
import com.au.module_android.DarkModeUtil

/**
 * 自动初始化
 */
object FirstInitial {
    var isInitSharedPrefHook = false

    /**
     * application初始化完成的通知。时机就是我们把基础的app全局给设置好。避免有的地方无法调用到。
     */
    val firstInitialOnCreateData = MutableLiveData<Any>()

    fun init(context: Context): Application {
        val app = context as Application
        ToutiaoScreenAdapter.init(app)
        app.registerActivityLifecycleCallbacks(GlobalActivityCallback())
        ProcessLifecycleOwner.get().lifecycle.addObserver(GlobalBackgroundCallback)

        Globals.internalApp = app

        if (isInitSharedPrefHook) {
            optimizeSpTask()
        }

        firstInitialOnCreateData.postValue(Unit)

        DarkModeUtil().initAppDarkMode(app)
        return app
    }
}