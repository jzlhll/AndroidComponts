package com.allan.androidlearning

import android.content.Intent
import android.os.Bundle
import com.allan.androidlearning.crashtest.debugEntryActivityCreateCrash
import com.allan.androidlearning.crashtest.debugEntryActivityPostMainThreadCrash
import com.allan.androidlearning.transfer.KEY_START_TYPE
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
        val startType = intent?.getStringExtra(KEY_START_TYPE)
        intent?.removeExtra(KEY_START_TYPE)
        startActivityFix(Intent(this, EntryActivity::class.java).also {
            it.putExtra(KEY_START_TYPE, startType)
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        logdNoFile(UncaughtExceptionHandlerObj.TAG) { "Splash on Create..." }
        debugEntryActivityCreateCrash()
        debugEntryActivityPostMainThreadCrash()
    }
}