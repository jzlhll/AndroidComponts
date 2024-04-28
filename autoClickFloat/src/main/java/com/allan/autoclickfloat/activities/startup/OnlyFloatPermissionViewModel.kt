package com.allan.autoclickfloat.activities.startup

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.allan.autoclickfloat.AllPermissionActivity
import com.au.module_android.permissions.hasFloatWindowPermission
import com.au.module_android.simplelivedata.NoStickLiveData

/**
 * @author allan
 * @date :2024/4/17 9:53
 * @description:
 */
open class OnlyFloatPermissionViewModel : ViewModel() {
    companion object {
        const val STATE_ALL_PERMISSION_ENABLE = 0
        const val STATE_NO_FLOAT_WINDOW = -2

        fun isFloatWindowEnabled(activity: Activity) = activity.hasFloatWindowPermission()
    }

    val allPermissionEnabled = NoStickLiveData<Int>()

    open fun getPermission(activity: AllPermissionActivity) {
        val floatWindowEnabled = isFloatWindowEnabled(activity)

        if (floatWindowEnabled) {
            allPermissionEnabled.setValueSafe(STATE_ALL_PERMISSION_ENABLE)
        } else {
            allPermissionEnabled.setValueSafe(STATE_NO_FLOAT_WINDOW)
        }
    }
}