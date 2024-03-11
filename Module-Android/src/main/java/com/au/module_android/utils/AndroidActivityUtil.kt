package com.au.module_android.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

fun Context.startActivityFix(intent: Intent, opts:Bundle? = null) {
    if (this !is Activity) {
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        ActivityCompat.startActivity(this, intent, opts)
    } catch (e:Exception) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Android 10 或更高版本
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        } else {
            // Android 10 以下版本
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        ActivityCompat.startActivity(this, intent, opts)
    }
}

fun Fragment.startActivityFix(intent: Intent, opts:Bundle? = null) {
    ActivityCompat.startActivity(requireContext(), intent, opts)
}

fun Fragment.startActivityFixSafe(intent: Intent, opts:Bundle? = null) {
    context?.let {
        ActivityCompat.startActivity(it, intent, opts)
    }
}