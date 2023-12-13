package com.au.module_android.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.Globals
import com.au.module_android.permissions.activity.ActivityForResult
import com.au.module_android.permissions.activity.IActivityResult
import com.au.module_android.permissions.permission.IPermissionResult
import com.au.module_android.permissions.permission.IMultiPermissionsResult
import com.au.module_android.permissions.permission.PermissionForResult
import com.au.module_android.permissions.permission.PermissionsForResult

const val REQUEST_OVERLAY_CODE: Int = 1001

/**
 * 多权限的申请
 */
fun createMultiPermissionForResult(
    permissions:Array<String>,
    onResultCallback:((Map<String, @JvmSuppressWildcards Boolean>) -> Unit)? = null)
        : IMultiPermissionsResult
    = PermissionsForResult(permissions, ActivityResultContracts.RequestMultiplePermissions(), onResultCallback)

/**
 * 单权限的申请
 */
fun createPermissionForResult(permission:String,
            onResultCallback:((Boolean)->Unit)? = null) : IPermissionResult
        = PermissionForResult(permission, ActivityResultContracts.RequestPermission(), onResultCallback)

/**
 * activity 跳转，返回拿结果。
 */
fun createActivityForResult(
    onResultCallback : ((ActivityResult)->Unit)? = null
) : IActivityResult
        = ActivityForResult(ActivityResultContracts.StartActivityForResult(), onResultCallback)

fun createTakePicForResult(onResu)

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
fun IActivityResult.jumpToAppDetail(appContext: Context, callback:(ActivityResult)->Unit) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", appContext.packageName, null)
    onResultCallback = callback
    start(intent, null)
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

/**
 * 使用
 * createMultiPermissionForResult(permissions)
 * createPermissionForResult(permission)
 * 创建，不用传入第二参数。
 *
 * 因为block放在了这里设置。
 */
fun <T> IResult.safeRun(block:()->Unit, notGivePermissionBlock:(()->Unit)? = null) {
    when (this) {
        is PermissionForResult -> {
            if(hasPermission(permission())) {
                block.invoke()
            } else {
                this.onResultCallback = {
                    if(it) block.invoke() else notGivePermissionBlock?.invoke()
                }
                this.start(null)
            }
        }

        is PermissionsForResult -> {
            if (hasPermission(*permissions())) {
                block.invoke()
            } else {
                this.onResultCallback = {
                    var hasPermission = false
                    for (entry in it) {
                        if (!entry.value) {
                            hasPermission = false
                            break
                        } else {
                            hasPermission = true
                        }
                    }
                    if(hasPermission) block.invoke() else notGivePermissionBlock?.invoke()
                }
                this.start(null)
            }
        }
    }
}