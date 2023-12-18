package com.au.module_android.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.Globals
import com.au.module_android.permissions.activity.ActivityForResult
import com.au.module_android.permissions.activity.IActivityResult
import com.au.module_android.permissions.other.TakePictureForResult
import com.au.module_android.permissions.other.TakePicturePreviewForResult
import com.au.module_android.permissions.permission.IPermissionResult
import com.au.module_android.permissions.permission.IMultiPermissionsResult
import com.au.module_android.permissions.permission.PermissionForResult
import com.au.module_android.permissions.permission.PermissionsForResult

const val REQUEST_OVERLAY_CODE: Int = 1001

/**
 * 多权限的申请
 */
fun createMultiPermissionForResult(permissions:Array<String>,
    onResultCallback:((Map<String, @JvmSuppressWildcards Boolean>) -> Unit)? = null)
        : IMultiPermissionsResult
    = PermissionsForResult(permissions) { onResultCallback?.invoke(it) }

/**
 * 单权限的申请
 */
fun createPermissionForResult(permission:String, onResultCallback:((Boolean)->Unit)? = null) : IPermissionResult
        = PermissionForResult(permission) { onResultCallback?.invoke(it) }

/**
 * activity 跳转，返回拿结果。
 */
fun createActivityForResult(
    onResultCallback : ((ActivityResult)->Unit)? = null) : IActivityResult
        = ActivityForResult(onResultCallback ?: {})

fun createActivityJumpToAppDetail(
    onResultCallback : ((ActivityResult)->Unit)? = null) : IActivityResult
    = ActivityForResult(onResultCallback ?: {})

/**
 * activity 跳转，返回拿结果。
 * @param uri 请使用AndroidUtils的方法来实现.getPictureFileUri.
 */
fun createTakePictureForResult(uri:Uri, onResultCallback:((Boolean)->Unit)? = null) : IResult<Boolean>
        = TakePictureForResult(uri) {onResultCallback?.invoke(it)}

/**
 * activity 跳转，返回拿结果。
 */
fun createTakeBitmapForResult(onResultCallback:((Bitmap?)->Unit)? = null) : IResult<Bitmap?>
        = TakePicturePreviewForResult {onResultCallback?.invoke(it)}

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

fun hasPermission(vararg permissions:String) : Boolean {
    return checkPermission(*permissions).isEmpty()
}

fun checkPermission(vararg permissions:String) : Array<String> {
    val noPermissionList = mutableListOf<String>()
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(Globals.app, permission) != PackageManager.PERMISSION_GRANTED) {
            noPermissionList.add(permission)
        }
    }
    return noPermissionList.toTypedArray()
}