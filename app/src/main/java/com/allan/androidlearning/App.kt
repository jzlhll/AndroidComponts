package com.allan.androidlearning

import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.InitApplication

/**
 * @author allan
 * @date :2024/9/29 16:42
 * @description:
 */
class App : InitApplication() {
    override fun initBeforeAttachBaseContext() {
        DarkModeAndLocalesConst.supportLocales = listOf(
            Locales.LOCALE_JIANTI_CN,
            Locales.LOCALE_FANTI_CN,
            Locales.LOCALE_US)
    }
}