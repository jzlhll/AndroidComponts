package com.au.module_android.permissions.media

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.ACTION_SYSTEM_FALLBACK_PICK_IMAGES
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
class NewMultiPhotoPickerForResult(cxt:Any, maxItem:Int, private val mediaType: VisualMediaType, isDynamicMaxItems:Boolean = false) :
    IContractResult<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>(cxt,
        if(isDynamicMaxItems) DynamicPickMultiVisualMedia(maxItem) else PickMultipleVisualMedia(maxItem)) {
    /**
     * 启动
     */
    fun request(callback: ActivityResultCallback<List<@JvmSuppressWildcards Uri>>?) {
        callback?.let { setResultCallback(it) }
        launcher.launch(PickVisualMediaRequest(mediaType))
    }
}

class DynamicPickMultiVisualMedia(var currentMaxItems:Int) : PickMultipleVisualMedia(2) //随便给一个，只是为了父类不报错。
{
    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return super.createIntent(context, input).apply {
            when (action) {
                MediaStore.ACTION_PICK_IMAGES -> {
                    putExtra("android.provider.extra.PICK_IMAGES_MAX", currentMaxItems)
                }
                ACTION_SYSTEM_FALLBACK_PICK_IMAGES,
                "com.google.android.gms.provider.action.PICK_IMAGES" -> {
                    putExtra("com.google.android.gms.provider.extra.PICK_IMAGES_MAX", currentMaxItems)
                }
            }
        }
    }
}

