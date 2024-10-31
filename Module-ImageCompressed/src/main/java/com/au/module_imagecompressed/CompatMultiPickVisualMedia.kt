package com.au.module_imagecompressed

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts

/**
 * 如果小于等于1就会无法弹出。因此，还得包裹一层。
 * 支持 >= 1
 */
class CompatMultiPickVisualMedia(max:Int) : ActivityResultContract<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>>() //随便给一个，只是为了父类不报错。
{
    private var pickMedia = ActivityResultContracts.PickVisualMedia()
    private var multiPickMedia = DynamicPickMultiVisualMedia(max) //later: 如果是1的话，其实不太对的。但是并没什么影响，不会被引用。暂时如此。

    private var currentMax = max

    fun setCurrentMaxItems(max:Int) {
        currentMax = max
        if (max > 1) {
            multiPickMedia.setCurrentMaxItems(max)
        }
    }

    override fun createIntent(context: Context, input: PickVisualMediaRequest): Intent {
        return if (currentMax == 1) pickMedia.createIntent(context, input) else multiPickMedia.createIntent(context, input)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): List<Uri> {
        if(currentMax == 1) {
            val uri = pickMedia.parseResult(resultCode, intent)
            return if (uri != null) {
                listOf(uri)
            } else {
                emptyList()
            }
        } else {
            return multiPickMedia.parseResult(resultCode, intent)
        }
    }

    override fun getSynchronousResult(context: Context, input: PickVisualMediaRequest): SynchronousResult<List<Uri>>? {
        return null //later: 2个都是null。统一为null。
    }
}