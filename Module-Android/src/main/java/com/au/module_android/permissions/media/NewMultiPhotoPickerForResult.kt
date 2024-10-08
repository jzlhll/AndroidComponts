package com.au.module_android.permissions.media

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.au.module_android.permissions.IContractResult

/**
 * 当初始化完成这个对象后，请在onCreate里面调用 函数（onCreate）即可
 */
class NewMultiPhotoPickerForResult(cxt:Any, maxItem:Int, private val mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType) :
    IContractResult<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>(cxt, ActivityResultContracts.PickMultipleVisualMedia(maxItem)) {
    /**
     * 启动
     */
    fun request(callback: ActivityResultCallback<List<@JvmSuppressWildcards Uri>>?) {
        callback?.let { setResultCallback(it) }
        launcher.launch(PickVisualMediaRequest(mediaType))
    }

}

