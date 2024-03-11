package com.au.jobstudy

import android.content.Intent
import com.au.module_android.utils.startActivityFix
import com.au.module_androidex.AbsSplashActivity

/**
 * @author allan
 * @date :2024/3/11 16:13
 * @description:
 */
class CompactSplashActivity : AbsSplashActivity() {
    override fun goActivity() {
        startActivityFix(Intent(this, MainActivity::class.java))
    }
}