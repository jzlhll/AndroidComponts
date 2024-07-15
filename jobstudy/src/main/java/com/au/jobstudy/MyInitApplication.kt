package com.au.jobstudy

import com.au.jobstudy.check.CheckConsts
import com.au.module_android.Globals
import com.au.module_android.init.GlobalBackgroundCallback
import com.au.module_android.init.InitApplication
import com.au.module_android.utils.launchOnThread
import com.au.module_android.utils.logd

/**
 * @author au
 * @date :2023/11/14 14:05
 * @description:
 */
class MyInitApplication : InitApplication() {
    override fun onCreate() {
        super.onCreate()
        GlobalBackgroundCallback.addListener {
            if (it) {
                logd { "update SummerConst when foreground" }
                Globals.mainScope.launchOnThread {
                    CheckConsts.whenTrigger()
                }
            }
        }
    }
}