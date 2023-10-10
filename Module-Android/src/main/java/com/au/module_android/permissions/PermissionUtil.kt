package com.au.module_android.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner

const val REQUEST_OVERLAY_CODE: Int = 1001

/**
 * 多权限的申请
 */
fun <O> createMultiPermissionForResult(
    onResultCallback:((Map<String, @JvmSuppressWildcards Boolean>) -> Unit)? = null)
        : IPermission<Array<String>, Map<String, @JvmSuppressWildcards Boolean>>
    = OwnerForResult(ActivityResultContracts.RequestMultiplePermissions(), onResultCallback)

/**
 * 单权限的申请
 */
fun createPermissionForResult(onResultCallback:((Boolean)->Unit)? = null)
: IPermission<String, Boolean>
        = OwnerForResult(ActivityResultContracts.RequestPermission(), onResultCallback)

/**
 * activity start使用的
 */
fun createActivityForResult(
    onResultCallback : ((ActivityResult)->Unit)? = null
) : IPermission<Intent, ActivityResult>
        = OwnerForResult(ActivityResultContracts.StartActivityForResult(), onResultCallback)

/**
 * 其他
 */
fun <I, O> createForResult(resultContract: ActivityResultContract<I, O>,
                           onResultCallback: ((O) -> Unit)? = null) : IPermission<I, O>{
    return OwnerForResult(resultContract, onResultCallback)
}

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
fun IPermission<Intent, ActivityResult>.jumpToAppDetail(appContext: Context, callback:(ActivityResult)->Unit) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
    intent.data = Uri.fromParts("package", appContext.packageName, null)
    onResultCallback = callback
    start(intent, null)
}
