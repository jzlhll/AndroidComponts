package com.allan.androidlearning.activities

import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.allan.androidlearning.BuildConfig
import com.allan.androidlearning.databinding.ActivityJsHtmlBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logd
import com.au.module_android.utilsmedia.UriHelper
import com.au.module_imagecompressed.CameraAndSelectPhotosPermissionHelper
import com.au.module_imagecompressed.CameraPermissionHelp
import com.au.module_imagecompressed.LubanCompress
import com.au.module_imagecompressed.MultiPhotoPickerContractResult
import com.au.module_imagecompressed.TakePhotoActionDialog
import com.au.module_imagecompressed.imageFileConvertToUriWrap
import java.io.File

/**
 * @author allan
 * @date :2024/12/5 9:40
 * @description:
 */
@EntryFrgName(priority = 200)
class WebUploadFragment : BindingFragment<ActivityJsHtmlBinding>(), TakePhotoActionDialog.ITakePhotoActionDialogCallback {

    private var selectValueCallback:(ValueCallback<Array<Uri>>)? = null

    val cameraAndSelectHelper = CameraAndSelectPhotosPermissionHelper(this, object : CameraPermissionHelp.Supplier {
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
        cameraAndSelectHelper.multiResult.launchByAll(cameraAndSelectHelper.pickerType, null) {uris->
            for (uri in uris) {
                logd { "web: seected pics: $uri" }
            }
            selectValueCallback?.onReceiveValue(uris.map { it.uri }.toTypedArray())
        }
    }

    override fun onClickTakePic() {
        cameraAndSelectHelper.cameraHelper.safeRunTakePic( {result, createdTmpFile->
            if (result) {
                LubanCompress().setResultCallback { srcPath, resultPath, isSuc ->
                    val r = if(isSuc) resultPath else srcPath
                    ignoreError {
                        val resultFile = File(r)
                        val resultUri = resultFile.toUri()
                        val cvtUri = UriHelper(resultUri, Globals.app.contentResolver).imageFileConvertToUriWrap()
                        logd { "web: cvtUri $cvtUri" }
                        selectValueCallback?.onReceiveValue(arrayOf(cvtUri.uri))
                    }
                }.compress(requireContext(), createdTmpFile.toUri()) //必须是file的scheme。那个FileProvider提供的则不行。
            }
        })
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
            cameraAndSelectHelper.apply {
                pickerType = if (hasImage) {
                    if (hasVideo) {
                        MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO
                    } else {
                        MultiPhotoPickerContractResult.PickerType.IMAGE
                    }
                } else {
                    if (hasVideo) {
                        MultiPhotoPickerContractResult.PickerType.VIDEO
                    } else {
                        MultiPhotoPickerContractResult.PickerType.IMAGE
                    }
                }

                if (capture == true || true) {
                    showPhotoAndCameraDialog(if (isMultiple) {
                        3
                    } else {
                        1
                    })
                } else {
                    onClickSelectPhoto()
                }
            }
        }
    }
}