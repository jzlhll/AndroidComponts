package com.allan.autoclickfloat.activities.startup

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.fragment.app.Fragment
import com.allan.autoclickfloat.BuildConfig
import com.allan.autoclickfloat.activities.startup.OnlyFloatPermissionViewModel.Companion.isFloatWindowEnabled
import com.au.module_android.permissions.gotoAccessibilityPermission
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_androidui.dialogs.ConfirmCenterDialog

/**
 * @author allan
 * @date :2024/4/17 9:53
 * @description:
 */
class PermissionsHelper {
    companion object {
        fun isAccessibilityEnabled(context: Context) : Boolean {
            val accessibilityMgr = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
            if (accessibilityMgr.isEnabled) {
                val enableServices = accessibilityMgr.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
                for (sv in enableServices) {
                    if (sv.resolveInfo.serviceInfo.packageName.equals(BuildConfig.APPLICATION_ID)) {
                        return true
                    }
                }
            }
            return false
        }

        fun showGotoSystemAccessibilityPermission(fragment: Fragment) : Boolean{
            val ac = fragment.requireActivity()
            if (!isAccessibilityEnabled(ac)) {
                ConfirmCenterDialog.show(fragment.childFragmentManager,
                    "请授予辅助权限",
                    "请点击确定，跳转去辅助服务，找到应用「AShoot辅助点击方案」，并建议开启快捷开关。如果已经存在快捷开关，则可以快捷开关开启。",
                    "确定") {
                    ac.gotoAccessibilityPermission()
                    it.dismissAllowingStateLoss()
                }
                return false
            }
            return true
        }

        fun showGotoSystemFloatWindowPermission(fragment: Fragment) : Boolean{
            val ac = fragment.requireActivity()
            if (!isFloatWindowEnabled(ac)) {
                ConfirmCenterDialog.show(fragment.childFragmentManager,
                    "请授予悬浮窗权限",
                    "请点击确定，跳转去设置，找到应用「AShoot辅助点击方案」开启悬浮窗权限。",
                    "确定") {
                    ac.gotoFloatWindowPermission()
                    it.dismissAllowingStateLoss()
                }
                return false
            }
            return true
        }
    }
}