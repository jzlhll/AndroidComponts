package com.au.module_android.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner

const val REQUEST_OVERLAY_CODE: Int = 1001

/**
 * 请求弹窗权限。
 */
fun LifecycleOwner.requestFloatWindowPermission(context: AppCompatActivity) {
    val version = true //Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    if (version && !Settings.canDrawOverlays(context)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + context.packageName))
        ActivityCompat.startActivityForResult(context, intent, REQUEST_OVERLAY_CODE, null)
    }
}

/**
* 跳转到本程序的详情设置处。
* afterBackAppBlock 表示跳转系统app详情后，回来以后，check权限使用，用户又会点击。所以一般不用管。
*/
fun IPermission<Intent, ActivityResult>.jumpToAppDetail(appContext: Context, afterBackAppBlock:((ActivityResult)->Unit)? = null) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", appContext.packageName, null)
    start(intent, null) {
        afterBackAppBlock?.invoke(it)
    }
}
