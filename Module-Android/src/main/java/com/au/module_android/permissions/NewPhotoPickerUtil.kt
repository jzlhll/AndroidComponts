package com.au.module_android.permissions

import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.permissions.media.NewPhotoPickerForResult

/**
 * android13+的实现。google要求不能再需要权限
 */
fun LifecycleOwner.newPhotoPickerForResult(mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType) = NewPhotoPickerForResult(this, mediaType)

/**
 * android13+的实现。google要求不能再需要权限
 */
fun LifecycleOwner.newMultiPhotoPickerForResult(mediaType: ActivityResultContracts.PickVisualMedia.VisualMediaType) = NewPhotoPickerForResult(this, mediaType)
