package com.allan.androidlearning.activities

import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.core.content.FileProvider
import com.allan.androidlearning.BuildConfig
import com.allan.androidlearning.databinding.ActivityJsHtmlBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd
import com.au.module_imagecompressed.CameraAndSelectPhotosPermissionHelper
import com.au.module_imagecompressed.CameraPermissionHelp
import com.au.module_imagecompressed.MultiPhotoPickerContractResult
import com.au.module_imagecompressed.TakePhotoActionDialog
import java.io.File

/**
 * @author allan
 * @date :2024/12/5 9:40
 * @description:
 */
@EntryFrgName(priority = 100)
class WebUploadFragment : BindingFragment<ActivityJsHtmlBinding>(), TakePhotoActionDialog.ITakePhotoActionDialogCallback {

    private var selectValueCallback:(ValueCallback<Array<Uri>>)? = null

    val cameraAndSelectHelper = CameraAndSelectPhotosPermissionHelper(this,
        supplier = object : CameraPermissionHelp.Supplier {
        override fun createFileProvider(): Pair<File, Uri> {
            return createFileProviderMine()
        }
    })

    private fun createFileProviderMine(): Pair<File, Uri> {
        val picture = File(Globals.goodCacheDir.path + "/shared")
        picture.mkdirs()
        val file = File(picture, "pic_" + System.currentTimeMillis() + ".jpg")
        val uri = FileProvider.getUriForFile(
            Globals.app,
            "${BuildConfig.APPLICATION_ID}.provider",
            file
        )
        return file to uri
    }

    override fun onClickSelectPhoto() {
        logd { "onClick SelectPhoto" }
        cameraAndSelectHelper.launchSelectPhotos {uris->
            logd { "launchSelectPhotos callback ${uris.size}" }
            for (uri in uris) {
                logd { "launchSelectPhotos callback uri: $uri" }
            }
            selectValueCallback?.onReceiveValue(uris.map { it.uri }.toTypedArray())
        }
    }

    override fun onClickTakePic() : Boolean{
        logd { "onClick TakePic" }
        return cameraAndSelectHelper.cameraHelper.safeRunTakePicMust(requireContext()){mode, uriWrap->
            logd { "on click take pic mode=>$mode" }
            if (uriWrap != null) {
                selectValueCallback?.onReceiveValue(arrayOf(uriWrap.uri))
            } else {
                selectValueCallback?.onReceiveValue(null)
            }
        }
    }

    override fun onTakeDialogClosed() {
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

            var pickerType = MultiPhotoPickerContractResult.PickerType.IMAGE
            if (hasImage && hasVideo) {
                pickerType = MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO
            } else if (hasVideo) {
                pickerType = MultiPhotoPickerContractResult.PickerType.VIDEO
            }

            if (capture == true || true) {
                val max = if (isMultiple) {
                    3
                } else {
                    1
                }
                cameraAndSelectHelper.showTakeActionDialog(max, pickerType)
            } else {
                onClickSelectPhoto()
            }
        }
    }
}