package com.au.module_imagecompressed

import android.net.Uri
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
import java.io.File

/**
 * 封装了请求camera的一系列动作。
 * 使用规则：直接在fragment或者Activity中全局变量申明。使用的时候，只有safeRun调用即可。
 */
class CameraPermissionHelp {
    interface Supplier {
        /**
         * 参考代码
         *             val picture = File(Globals.goodCacheDir.path)
         *             picture.mkdirs()
         *             val file = File(picture, "pic_" + System.currentTimeMillis() + ".jpg")
         *             val uri = FileProvider.getUriForFile(
         *                 Globals.app,
         *                 "${applicationId}.fileprovider",
         *                 file
         *             )
         *             return file to uri
         */
        fun createFileProvider() : Pair<File, Uri>
    }

    private val f: Fragment?
    private val fa: FragmentActivity?
    private val supplier : Supplier

    private val permissionStr = android.Manifest.permission.CAMERA
    private val cameraPermissionResult: IOnePermissionResult
    private val takePictureForResult:SystemTakePictureForResult

    private val realActivity: FragmentActivity
        get() = f?.requireActivity() ?: fa!!

    constructor(f: Fragment, supplier:Supplier) {
        this.f = f
        this.supplier = supplier
        cameraPermissionResult = f.createPermissionForResult(permissionStr)
        takePictureForResult = f.systemTakePictureForResult()
        fa = null
    }

    constructor(fa: FragmentActivity, supplier:Supplier) {
        this.fa = fa
        this.supplier = supplier
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

    private fun realRunTakePic(callback:(Boolean, createdTmpFile: File)->Unit?) {
        val (createdTmpFile, uri) = supplier.createFileProvider()
        takePictureForResult.start(uri) {
            callback(it, createdTmpFile)
        }
    }

    /**
     * 注意offerBlock代表着，得到权限以后，你需要提供的共享文件，file_path.xml uri和callback
     */
    fun safeRunTakePic(callback:(Boolean, createdTmpFile: File)->Unit?, errorBlock:()->Unit = {
        ToastBuilder().setOnTop().setIcon("info").setMessage("需要camera权限.").toast()
    }) {
        if (isGrant()) {
            realRunTakePic(callback)
        } else {
            if (canShowRequestDialogUi(realActivity)) {
                cameraPermissionResult.safeRun(block = {
                    realRunTakePic(callback)
                }, notGivePermissionBlock = {
                    errorBlock()
                })
            } else {
                errorBlock()
            }
        }
    }
}