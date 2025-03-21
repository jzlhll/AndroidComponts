package com.allan.autoclickfloat.activities.autofs

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import com.au.module_android.utils.startActivityFix
import java.util.Locale

fun goToManageSetting(context: Context) {
        context.startActivityFix(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS))
    }

/**
 * 是否有权限。
 */
fun canWrite(context: Context) = Settings.System.canWrite(context)

fun goToAutoStartSettings(context: Context) : Boolean{
    var goToSuc = true

    try {
        val intent = Intent()
        val manufacturer = Build.MANUFACTURER.lowercase(Locale.getDefault())

        when {
            // 华为
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> {
                intent.component = ComponentName(
                    "com.huawei.systemmanager",
                    "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                )
            }
            // 小米
            manufacturer.contains("xiaomi") -> {
                intent.component = ComponentName(
                    "com.miui.securitycenter",
                    "com.miui.permcenter.autostart.AutoStartManagementActivity"
                )
            }
            // OPPO
            manufacturer.contains("oppo") -> {
                intent.component = ComponentName(
                    "com.coloros.safecenter",
                    "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                )
            }
            // vivo
            manufacturer.contains("vivo") -> {
                intent.component = ComponentName(
                    "com.vivo.perm",
                    "com.vivo.perm.ui.autostart.AutoStartManagerActivity"
                )
            }
            // 其他设备跳转通用应用信息页
            else -> {
//                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
//                intent.data = "package:${context.packageName}".toUri()
                goToSuc = false
            }
        }

        // 尝试跳转
        context.startActivityFix(intent)
    } catch (e: Exception) {
        // 跳转失败时回退到通用页
//        val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
//            data = "package:${context.packageName}".toUri()
//        }
//        context.startActivityFix(fallbackIntent)
        goToSuc = false
    }

    return goToSuc
}
