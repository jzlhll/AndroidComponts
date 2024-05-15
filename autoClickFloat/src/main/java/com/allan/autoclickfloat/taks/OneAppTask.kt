package com.allan.autoclickfloat.taks

import android.content.Context
import com.au.module_android.utils.startActivityFix

/**
 * @author allan
 * @date :2024/5/15 10:55
 * @description:
 */
abstract class OneAppTask(private val context:Context, private val appPackageName:String) {
    open fun startMainAndWait(waitTs:Long) : Boolean{
        val it = context.packageManager.getLaunchIntentForPackage(appPackageName) ?: return false
        context.startActivityFix(it)
        return true
    }
}