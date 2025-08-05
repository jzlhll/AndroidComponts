package com.au.module_imagecompressed

import android.content.Context
import android.net.Uri
import androidx.core.app.ActivityCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.au.module_android.Globals
import com.au.module_android.permissions.activity.SystemTakePictureForResult
import com.au.module_android.permissions.createPermissionForResult
import com.au.module_android.permissions.hasPermission
import com.au.module_android.permissions.permission.IOnePermissionResult
import com.au.module_android.permissions.systemTakePictureForResult
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.ignoreError
import com.au.module_android.utilsmedia.UriHelper
import com.au.module_androidui.toast.ToastBuilder
import java.io.File

/**
 * 封装了请求camera的一系列动作。
 * 使用规则：直接在fragment或者Activity中全局变量申明。使用的时候，只有safeRun调用即可。
 */
class CameraPermissionHelp {

    /**
     * 文件提供者的接口FileProvider.getUriForFile(
     * 参考createFileProvider()注释
     */
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

    private fun realRunTakePic(callback:(createdTmpFile: File?)->Unit?) {
        val (createdTmpFile, uri) = supplier.createFileProvider()
        takePictureForResult.start(uri) {
            if (it) {
                callback(createdTmpFile)
            } else {
                callback(null)
            }
        }
    }

    /**
     * @param callback 代表拍照成功后的回调，成功就有文件，失败就没有文件
     * @param errorToastBlock 错误提示
     * @return 0表示正常；
     *          -1表示弹出权限弹窗(不要做什么事情因为是异步的选择框, 可以在notGivenPermissionExtraBlock里面做点事情)；
     *          -2表示无法弹出并直接拒绝( 推荐做一下跳转jumpToAppDetail)
     */
    private fun safeRunTakePic(callback:(createdTmpFile: File?)->Unit?,
                               notGivePermissionBlock:(()->Unit)? = null) : Int{
        if (isGrant()) {
            realRunTakePic(callback)
            return 0
        } else {
            if (canShowRequestDialogUi(realActivity)) {
                cameraPermissionResult.safeRun(block = {
                    realRunTakePic(callback)
                }, notGivePermissionBlock)
                return -1
            } else {
                return -2
            }
        }
    }

    /**
     * 相比safeRunTakePic，是必定callback有回调的。适用于H5WebView请求必须有回调回去的场景
     * @param  callback 一定有回调。null就是失败或者就是没有拍照回来。
     * @return 返回true表示拍照无法弹出授权。返回false则一定是能弹窗或者直接拍照去了。
     */
    fun safeRunTakePicMust(context: Context,
                                   needLubanCompress:Boolean = true,
                                   errorToastBlock:()->Unit = {ToastBuilder().setOnTop().setIcon("info").setMessage("需要camera权限.").toast() },
                                   callback: (mode:String, uriWrap: UriWrap?)->Unit) : Boolean{
        val ret = safeRunTakePic({createdTmpFile->
            if (createdTmpFile != null) {
                val createdTmpUri = createdTmpFile.toUri()
                if (needLubanCompress) {
                    //luban压缩
                    LubanCompress().setResultCallback { srcPath, resultPath, isSuc ->
                        val afterCompressPath = if(isSuc) resultPath else srcPath
                        if (afterCompressPath != null) {
                            ignoreError {
                                val afterCompressFile = File(afterCompressPath)
                                val cvtUri = UriHelper(afterCompressFile.toUri(), Globals.app.contentResolver).imageFileConvertToUriWrap()
                                callback("takePicResultLubanCompressed", cvtUri)
                            }
                        } else {
                            callback("takePicResultLubanCompressedError", null)
                        }
                    }.compress(context, createdTmpUri) //必须是file的scheme。那个FileProvider提供的则不行。
                } else {
                    //不压缩
                    val cvtUri = UriHelper(createdTmpUri, Globals.app.contentResolver).imageFileConvertToUriWrap()
                    callback("takePicResultDirect", cvtUri)
                }
            } else {
                callback("takePicNoResult", null)
            }
        }, notGivePermissionBlock = {
            errorToastBlock()
            callback("notGivePermission", null)
        })
        if (ret == -2) {
            errorToastBlock()
            callback("permissionRejectDirect", null)
        }

        return ret == -2
    }
}