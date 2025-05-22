package com.allan.androidlearning

import com.allan.androidlearning.crashtest.debugApplicationCreateCrash
import com.allan.androidlearning.crashtest.debugApplicationPostMainThreadCrash
import com.allan.androidlearning.crashtest.debugSubThreadCrash
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.cacheImportCopyDir
import com.allan.androidlearning.transfer.nanoTempCacheChunksDir
import com.au.module_android.DarkModeAndLocalesConst
import com.au.module_android.Globals
import com.au.module_android.InitApplication
import com.au.module_android.utils.clearDirOldFiles
import com.au.module_android.utils.launchOnIOThread
import java.util.Locale

/**
 * @author allan
 * @date :2024/9/29 16:42
 * @description:
 */
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
        registerActivityLifecycleCallbacks(MyDroidGlobalService)

        //一上来直接强制移除所有临时import的文件。
        Globals.mainScope.launchOnIOThread {
            clearDirOldFiles(cacheImportCopyDir(), 0)
        }
    }
}