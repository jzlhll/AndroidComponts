package com.allan.mydroid

import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.globals.cacheImportCopyDir
import com.au.module_android.Globals
import com.au.module_android.InitApplication
import com.au.module_android.utils.clearDirOldFiles
import com.au.module_android.utils.launchOnIOThread

/**
 * @author allan
 * @date :2024/9/29 16:42
 * @description:
 */
class App : InitApplication() {
    override fun initBeforeAttachBaseContext() {
    }
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(MyDroidGlobalService)

        //一上来直接强制移除所有临时import的文件。
        Globals.mainScope.launchOnIOThread {
            clearDirOldFiles(cacheImportCopyDir(), 0)
        }
    }
}