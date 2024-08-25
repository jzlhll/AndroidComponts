package com.au.module_android.permissions

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.Globals
import com.au.module_android.permissions.activity.ActivityForResult
import com.au.module_android.permissions.activity.SystemTakePictureForResult
import com.au.module_android.permissions.activity.SystemTakeVideoFaceForResult
import com.au.module_android.permissions.activity.SystemTakeVideoForResult
import com.au.module_android.permissions.permission.IMultiPermissionsResult
import com.au.module_android.permissions.permission.IOnePermissionResult
import com.au.module_android.permissions.permission.PermissionForResult
import com.au.module_android.permissions.permission.PermissionsForResult
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.startActivityFix

const val REQUEST_OVERLAY_CODE: Int = 1001
//todo https://article.juejin.cn/post/7082314521284444173 增加拍照等

/**
 * 无需权限就可以拉起系统拍照。
 */
fun LifecycleOwner.systemTakePictureForResult() = SystemTakePictureForResult(this)
/**
 * 无需权限就可以拉起系统录像。
 */
fun LifecycleOwner.systemTakeVideoForResult() = SystemTakeVideoForResult(this)

fun LifecycleOwner.systemTakeVideo2FrontForResult(isFront:Boolean = false,
                                                       maxSec:Int = 60,
                                                       isLowQuality:Boolean = true)
        = SystemTakeVideoFaceForResult(this, isFront, maxSec, isLowQuality)

/**
 * 多权限的申请
 */
fun LifecycleOwner.createMultiPermissionForResult(permissions:Array<String>)
        : IMultiPermissionsResult
    = PermissionsForResult(this, permissions)

/**
 * 单权限的申请
 */
fun LifecycleOwner.createPermissionForResult(permission:String) : IOnePermissionResult
        = PermissionForResult(this, permission)

/**
 * activity 跳转，返回拿结果。
 */
fun LifecycleOwner.createActivityForResult() : ActivityForResult = ActivityForResult(this)
/**
 * 跳转到辅助服务
 */
fun LifecycleOwner.gotoAccessibilityPermission() {
    val activity = when (this) {
        is Fragment -> requireActivity()
        is AppCompatActivity -> this
        else -> {
            throw IllegalArgumentException("gotoAccessibilityPermission error call.")
        }
    }
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    activity.startActivityFix(intent)
}

/**
 * 请求弹窗权限。
 */
fun LifecycleOwner.gotoFloatWindowPermission() {
    val version = true //Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    val activity = when (this) {
        is Fragment -> requireActivity()
        is AppCompatActivity -> this
        else -> {
            throw IllegalArgumentException("requestFloatWindowPermission error call.")
        }
    }
    if (version && !Settings.canDrawOverlays(activity)) {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:" + activity.packageName))
        ActivityCompat.startActivityForResult(activity, intent, REQUEST_OVERLAY_CODE, null)
    }
}

/**
 * 请求弹窗权限。
 */
fun Context.hasFloatWindowPermission() : Boolean{
    return Settings.canDrawOverlays(this)
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

fun canShowPermissionDialog(activity:Activity, permission:String) : Boolean{
    return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
}

fun viewToActivity(view: View) = view.context.asOrNull<AppCompatActivity>()