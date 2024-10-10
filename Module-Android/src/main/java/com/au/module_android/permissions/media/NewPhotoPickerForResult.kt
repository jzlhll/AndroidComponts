package com.au.module_android.permissions.media

import android.net.Uri
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VisualMediaType
import com.au.module_android.permissions.IContractResult

/**
 * 当初始化完成这个对象后，请在onCreate里面调用 函数（onCreate）即可
 */
class NewPhotoPickerForResult(cxt:Any, private val mediaType: VisualMediaType) :
    IContractResult<PickVisualMediaRequest, Uri?>(cxt, PickVisualMedia()) {

    /**
     * 启动
     */
    fun request(callback: ActivityResultCallback<Uri?>?) {
        callback?.let { setResultCallback(it) }
        launcher.launch(PickVisualMediaRequest(mediaType))
    }
}

/**
 * 当初始化完成这个对象后，请在onCreate里面调用 函数（onCreate）即可
 */
class NewMultiPhotoPickerForResult(cxt:Any, maxItem:Int, private val mediaType: VisualMediaType) :
    IContractResult<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>(cxt, PickMultipleVisualMedia(maxItem)) {
    /**
     * 启动
     */
    fun request(callback: ActivityResultCallback<List<@JvmSuppressWildcards Uri>>?) {
        callback?.let { setResultCallback(it) }
        launcher.launch(PickVisualMediaRequest(mediaType))
    }

}

