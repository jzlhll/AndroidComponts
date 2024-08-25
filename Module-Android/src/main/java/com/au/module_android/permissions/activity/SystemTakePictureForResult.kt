package com.au.module_android.permissions.activity

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import com.au.module_android.permissions.permission.IPermissionResult

/**
 * 当初始化完成这个对象后，请在onCreate里面调用 函数（onCreate）即可
 */
class SystemTakePictureForResult(owner:Any) : IPermissionResult<Uri, Boolean>(owner, ActivityResultContracts.TakePicture()) {
    /**
     * 启动activity
     */
    fun start(uri:Uri, callback: ActivityResultCallback<Boolean>?) {
        callback?.let { setResultCallback(it) }
        launcher.launch(uri)
    }
}