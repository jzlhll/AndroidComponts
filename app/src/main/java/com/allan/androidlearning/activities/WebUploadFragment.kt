package com.allan.androidlearning.activities

import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import com.allan.androidlearning.BuildConfig
import com.allan.androidlearning.databinding.ActivityJsHtmlBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd
import com.au.module_androidui.dialogs.TakePhotoActionDialog
import com.au.module_imagecompressed.MultiPhotoPickerContractResult
import com.au.module_imagecompressed.TakeAndSelectMediaPermissionHelper

/**
 * @author allan
 * @date :2024/12/5 9:40
 * @description:
 */
@EntryFrgName
class WebUploadFragment : BindingFragment<ActivityJsHtmlBinding>(), TakePhotoActionDialog.ITakePhotoActionDialogCallback {
    private var selectValueCallback:(ValueCallback<Array<Uri>>)? = null

    private val takeAndSelectMediaHelper = TakeAndSelectMediaPermissionHelper(this,
        BuildConfig.APPLICATION_ID,
        5).also {
        it.allResultsAction = { uriWraps->
            selectValueCallback?.onReceiveValue(uriWraps.map { it.uri }.toTypedArray())
            selectValueCallback = null
        }
    }

    override fun onClickSelectPhoto() {
        takeAndSelectMediaHelper.onClickSelectPhoto()
    }

    override fun onClickTakePic() {
        takeAndSelectMediaHelper.onClickTakePic()
    }

    override fun onNothingClosed() {
        selectValueCallback?.onReceiveValue(arrayOf())
        selectValueCallback = null
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.webView.loadUrl("file:///android_asset/webupload/upload.html")
        binding.webView.setSelectPictureAction { valueCallback, fileChooserParams ->
            selectValueCallback = valueCallback
            val acceptTypes = fileChooserParams?.acceptTypes
            val capture = fileChooserParams?.isCaptureEnabled
            val mode = fileChooserParams?.mode
            val isMultiple = mode == WebChromeClient.FileChooserParams.MODE_OPEN_MULTIPLE
            logd{ "web: capture $capture mode $mode isMultiple $isMultiple" }
            var hasImage = false
            var hasVideo = false
            if (!acceptTypes.isNullOrEmpty()) {
                for (type in acceptTypes) {
                    logd{"web: acceptTypes $type"}
                    if (type.contains("image/")) {
                        hasImage = true
                    }
                    if (type.contains("video/")) {
                        hasVideo = true
                    }
                }
            }
            if (hasImage) {
                if (hasVideo) {
                    takeAndSelectMediaHelper.pickerType = MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO
                } else {
                    takeAndSelectMediaHelper.pickerType = MultiPhotoPickerContractResult.PickerType.IMAGE
                }
            } else {
                if (hasVideo) {
                    takeAndSelectMediaHelper.pickerType = MultiPhotoPickerContractResult.PickerType.VIDEO
                } else {
                    takeAndSelectMediaHelper.pickerType = MultiPhotoPickerContractResult.PickerType.IMAGE
                }
            }

            if (capture == true || true) {
                takeAndSelectMediaHelper.changeMultiPickerForResultMaxNum(
                    if (isMultiple) {
                        takeAndSelectMediaHelper.picMaxSize
                    } else {
                        1
                    })
                takeAndSelectMediaHelper.showPhotoAndCameraDialog()
            } else {
                takeAndSelectMediaHelper.onClickSelectPhoto()
            }
            true
        }
    }
}