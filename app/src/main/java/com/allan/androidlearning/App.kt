package com.allan.androidlearning

import com.allan.androidlearning.crashtest.debugApplicationCreateCrash
import com.allan.androidlearning.crashtest.debugApplicationPostMainThreadCrash
import com.allan.androidlearning.crashtest.debugSubThreadCrash
import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.InitApplication
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

/**
 * @author allan
 * @date :2024/9/29 16:42
 * @description:
 */
@HiltAndroidApp
class App : InitApplication() {
    override fun initBeforeAttachBaseContext() {
        DarkModeAndLocalesConst.supportLocales = mapOf(
            Locales.LOCALE_JIANTI_CN_KEY to Locale.SIMPLIFIED_CHINESE,
            Locales.LOCALE_FANTI_CN_KEY to Locale.TRADITIONAL_CHINESE,
            Locales.LOCALE_US_KEY to Locale.ENGLISH,
        )
    }

    override fun onCreate() {
        super.onCreate()

        debugApplicationCreateCrash()
        debugApplicationPostMainThreadCrash()
        debugSubThreadCrash()
    }
}