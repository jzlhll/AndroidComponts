package com.allan.autoclickfloat.activities.startup

import android.app.Activity
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.lifecycle.ViewModel
import com.allan.autoclickfloat.AllPermissionActivity
import com.au.module_android.permissions.hasFloatWindowPermission
import com.au.module_android.simplelivedata.NoStickLiveData

/**
 * @author allan
 * @date :2024/4/17 9:53
 * @description:
 */
class PermissionsViewModel : ViewModel() {
    companion object {
        const val STATE_ALL_PERMISSION_ENABLE = 0
        const val STATE_ALL_NO_PERMISSION = -10
        const val STATE_NO_ACCESSIBILITY = -1
        const val STATE_NO_FLOAT_WINDOW = -2

        fun isAccessibilityEnabled(context: Context) : Boolean {
            val accessibilityMgr = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            return accessibilityMgr.isEnabled
        }

        fun isFloatWindowEnabled(activity: Activity) = activity.hasFloatWindowPermission()
    }

    val allPermissionEnabled = NoStickLiveData<Int>()

    fun getPermission(activity: AllPermissionActivity) {
        val floatWindowEnabled = isFloatWindowEnabled(activity)
        val accessEnabled = isAccessibilityEnabled(activity)

        if (floatWindowEnabled && accessEnabled) {
            allPermissionEnabled.setValueSafe(STATE_ALL_PERMISSION_ENABLE)
        } else if (floatWindowEnabled) {
            allPermissionEnabled.setValueSafe(STATE_NO_ACCESSIBILITY)
        } else if (accessEnabled) {
            allPermissionEnabled.setValueSafe(STATE_NO_FLOAT_WINDOW)
        } else {
            allPermissionEnabled.setValueSafe(STATE_ALL_NO_PERMISSION)
        }
    }
}