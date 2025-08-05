package com.au.module_imagecompressed

import androidx.fragment.app.Fragment

/**
 * google新要求：尽量不要使用自定义的图片选择器，使用系统的。
 * 请求多张系统图片或视频

 */
fun Fragment.compatMultiPhotoPickerForResult(maxItem:Int)
        = if(maxItem > 0)
            MultiPhotoPickerContractResult(this, maxItem, CompatMultiPickVisualMedia(maxItem))
          else throw RuntimeException("max item must > 0")

/**
 * google新要求：尽量不要使用自定义的图片选择器，使用系统的。
 * 请求一张系统图片或者视频
 */
fun Fragment.photoPickerForResult() = MultiPhotoPickerContractResult(this, 1, CompatMultiPickVisualMedia(1))