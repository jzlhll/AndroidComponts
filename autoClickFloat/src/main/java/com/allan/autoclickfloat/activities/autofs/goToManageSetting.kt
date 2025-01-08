package com.allan.autoclickfloat.activities.autofs

import android.content.Context
import android.content.Intent
import android.provider.Settings
import com.au.module_android.utils.startActivityFix

fun goToManageSetting(context: Context) {
        context.startActivityFix(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
    }

/**
 * 是否有权限。
 */
fun canWrite(context: Context) = Settings.System.canWrite(context)