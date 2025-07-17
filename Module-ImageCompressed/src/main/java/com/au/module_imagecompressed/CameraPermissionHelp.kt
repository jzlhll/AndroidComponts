package com.au.module_imagecompressed

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.au.module_android.Globals
import com.au.module_android.permissions.activity.SystemTakePictureForResult
import com.au.module_android.permissions.createPermissionForResult
import com.au.module_android.permissions.hasPermission
import com.au.module_android.permissions.permission.IOnePermissionResult
import com.au.module_android.permissions.systemTakePictureForResult
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_androidui.toast.ToastBuilder

/**
 * 封装了请求camera的一系列动作。
 * 使用规则：直接在fragment或者Activity中全局变量申明。使用的时候，只有safeRun调用即可。
 */
class CameraPermissionHelp {
    private val f: Fragment?
    private val fa: FragmentActivity?

    private val permissionStr = android.Manifest.permission.CAMERA
    private val cameraPermissionResult: IOnePermissionResult
    private val takePictureForResult:SystemTakePictureForResult

    private val realActivity: FragmentActivity
        get() = f?.requireActivity() ?: fa!!

    constructor(f: Fragment) {
        this.f = f
        cameraPermissionResult = f.createPermissionForResult(permissionStr)
        takePictureForResult = f.systemTakePictureForResult()
        fa = null
    }

    constructor(fa: FragmentActivity) {
        this.fa = fa
        cameraPermissionResult = fa.createPermissionForResult(permissionStr)
        takePictureForResult = fa.systemTakePictureForResult()
        this.f = null
    }

    private fun isGrant(): Boolean {
        return hasPermission(permissionStr)
    }

    /**
     *  功能描述： 是否允许展示权限弹窗
     **/
    private fun canShowRequestDialogUi(activity: FragmentActivity): Boolean {
        //第一次请求权限
        if (SharedPrefUtil.getBoolean(Globals.app, "cameraPermissionFirstRequest", true)) {
            SharedPrefUtil.putBoolean(Globals.app, "cameraPermissionFirstRequest", false)
            return true
        }

        // 1.从来没有申请过 false
        // 2.第一次请求被拒绝 true
        // 3.第一次请求被拒绝 允许权限 false
        // 4.禁止权限询问 false
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionStr)) {
            return true
        }
        return false
    }

    fun safeRunTakePic(offerBlock:()->Pair<Uri, ActivityResultCallback<Boolean>?>, errorBlock:()->Unit = {
        ToastBuilder().setOnTop().setIcon("info").setMessage("需要camera权限.").toast()
    }) {
        val sucBlock = {
            val pair = offerBlock()
            val uri = pair.first
            val callback = pair.second
            takePictureForResult.start(uri, callback)
        }

        if (isGrant()) {
            sucBlock()
        } else {
            if (canShowRequestDialogUi(realActivity)) {
                cameraPermissionResult.safeRun(block = {
                    sucBlock()
                }, notGivePermissionBlock = {
                    errorBlock()
                })
            } else {
                errorBlock()
            }
        }
    }
}