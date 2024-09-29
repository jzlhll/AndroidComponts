package com.allan.androidlearning

import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.InitApplication
import java.util.Locale

/**
 * @author allan
 * @date :2024/9/29 16:42
 * @description:
 */
class App : InitApplication() {
    override fun doBeforeAttachBaseContext() {
        DarkModeAndLocalesConst.supportLanguage = listOf(Locale.CHINESE, Locale.TAIWAN, Locale.US)
        DarkModeAndLocalesConst.supportLocaleFeature = true
        DarkModeAndLocalesConst.supportDarkModeFeature = true
    }
}