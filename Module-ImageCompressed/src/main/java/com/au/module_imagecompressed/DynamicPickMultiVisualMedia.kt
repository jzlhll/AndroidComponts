package com.au.module_imagecompressed

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.ACTION_SYSTEM_FALLBACK_PICK_IMAGES
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.Companion.EXTRA_SYSTEM_FALLBACK_PICK_IMAGES_MAX

/**
 * 支持 > 1
 * 如果需要支持 >=1 使用compat版本
 */
@Deprecated("instead with CompatMultiPickVisualMedia.")
class DynamicPickMultiVisualMedia(currentMaxItems:Int) : ActivityResultContracts.PickMultipleVisualMedia(2) //随便给一个，只是为了父类不报错。
{
    private var mCurrentMax = currentMaxItems
    fun setCurrentMaxItems(max:Int) {
        if (max == 1) {
            throw RuntimeException("max must > 1.")
        }
        mCurrentMax = max
    }

    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return super.createIntent(context, input).apply {
            when (action) {
                MediaStore.ACTION_PICK_IMAGES -> {
                    putExtra("android.provider.extra.PICK_IMAGES_MAX", mCurrentMax)
                }
                ACTION_SYSTEM_FALLBACK_PICK_IMAGES -> {
                    putExtra(EXTRA_SYSTEM_FALLBACK_PICK_IMAGES_MAX, mCurrentMax)
                }
                "com.google.android.gms.provider.action.PICK_IMAGES" -> {
                    putExtra("com.google.android.gms.provider.extra.PICK_IMAGES_MAX", mCurrentMax)
                }
            }
        }
    }
}