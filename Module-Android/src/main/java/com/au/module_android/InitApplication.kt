package com.au.module_android

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.EmptySuper
import com.au.module_android.utils.logdNoFile

/**
 * @author au
 * @date :2023/11/7 14:32
 * @description: 使用InitApplication做为基础的application父类或者直接使用
 */
open class InitApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        logdNoFile("InitApplication") { "init application onCreate" }
        FirstInitial().init(this)
        DarkModeAndLocalesConst.appOnCreated(this)
    }

    final override fun attachBaseContext(base: Context?) {
        logdNoFile("InitApplication") { "init application attach BaseContext" }
        initBeforeAttachBaseContext()
        super.attachBaseContext(DarkModeAndLocalesConst.appAttachBaseContext(base))
    }

    @EmptySuper
    open fun initBeforeAttachBaseContext() {}

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        DarkModeAndLocalesConst.appOnConfigurationChanged(this, newConfig)
    }

    override fun getResources() : Resources {
        if (BuildConfig.SUPPORT_LOCALES || BuildConfig.SUPPORT_DARKMODE) {
            //会影响Activity的获取。必须将处理后的resource替换掉。
            return DarkModeAndLocalesConst.themedContext?.resources ?: return super.getResources()
        }
        return super.getResources()
    }
}