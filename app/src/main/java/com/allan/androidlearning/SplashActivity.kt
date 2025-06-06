package com.allan.androidlearning

import android.content.Intent
import android.os.Bundle
import com.allan.androidlearning.crashtest.debugEntryActivityCreateCrash
import com.allan.androidlearning.crashtest.debugEntryActivityPostMainThreadCrash
import com.au.module_android.crash.UncaughtExceptionHandlerObj
import com.au.module_android.init.AbsSplashActivity
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.startActivityFix

/**
 * @author allan
 * @date :2024/11/20 15:07
 * @description:
 */
class SplashActivity : AbsSplashActivity() {
    override fun goActivity(intent: Intent?) {
        startActivityFix(Intent(this, EntryActivity::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logdNoFile(UncaughtExceptionHandlerObj.TAG) { "Splash on Create..." }
        debugEntryActivityCreateCrash()
        debugEntryActivityPostMainThreadCrash()
    }
}