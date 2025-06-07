package com.allan.mydroid

import android.content.Intent
import androidx.core.os.bundleOf
import com.allan.mydroid.globals.KEY_START_TYPE
import com.allan.mydroid.views.MyDroidAllFragment
import com.au.module_android.init.AbsSplashActivity
import com.au.module_android.ui.FragmentShellActivity

/**
 * @author allan
 * @date :2024/11/20 15:07
 * @description:
 */
class SplashActivity : AbsSplashActivity() {
    override fun goActivity(intent: Intent?) {
        val startTypeValue = intent?.getStringExtra(KEY_START_TYPE)
        if (startTypeValue == null) {
            FragmentShellActivity.start(this, MyDroidAllFragment::class.java)
        } else {
            intent.removeExtra(KEY_START_TYPE)
            FragmentShellActivity.start(this, MyDroidAllFragment::class.java,
                bundleOf(KEY_START_TYPE to startTypeValue)
            )
        }
    }
}