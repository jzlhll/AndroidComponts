package com.au.module_android.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.annotation.AnimRes
import androidx.fragment.app.Fragment
import com.au.module_android.R

fun Context.startActivityFix(intent: Intent, opts:Bundle? = null, @AnimRes enterAnim:Int? = null) {
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        startActivity(intent, opts)
    } catch (e:Exception) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 10 或更高版本
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            // Android 10 以下版本
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        startActivity(intent, opts)
    }

    if (enterAnim != null && this is Activity) {
        this.overridePendingTransition(enterAnim, R.anim.activity_stay)
    }
}

fun Fragment.startActivityFix(intent: Intent, opts:Bundle? = null, @AnimRes enterAnim:Int? = null) {
    requireContext().startActivityFix(intent, opts, enterAnim)
}

fun Context.startOutActivity(intent: Intent, opts:Bundle? = null, @AnimRes enterAnim:Int? = null) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        // Android 10 或更高版本
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    } else {
        // Android 10 以下版本
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
    }
    try {
        startActivity(intent, opts)
    } catch (e:Exception) {
        e.printStackTrace()
    }

    if (enterAnim != null && this is Activity) {
        this.overridePendingTransition(enterAnim, R.anim.activity_stay)
    }
}