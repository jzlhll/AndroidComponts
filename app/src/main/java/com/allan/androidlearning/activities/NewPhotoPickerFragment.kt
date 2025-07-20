package com.allan.androidlearning.activities

import android.net.Uri
import android.os.Bundle
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.allan.androidlearning.BuildConfig
import com.allan.androidlearning.databinding.FragmentPhotoPickerBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.glide.glideSetAny
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logd
import com.au.module_android.utilsmedia.UriHelper
import com.au.module_imagecompressed.CameraAndSelectPhotosPermissionHelper
import com.au.module_imagecompressed.CameraPermissionHelp
import com.au.module_imagecompressed.LubanCompress
import com.au.module_imagecompressed.MultiPhotoPickerContractResult
import com.au.module_imagecompressed.TakePhotoActionDialog
import com.au.module_imagecompressed.UriWrap
import com.au.module_imagecompressed.compatMultiPhotoPickerForResult
import com.au.module_imagecompressed.imageFileConvertToUriWrap
import com.au.module_imagecompressed.photoPickerForResult
import java.io.File

@EntryFrgName
class NewPhotoPickerFragment : BindingFragment<FragmentPhotoPickerBinding>(), TakePhotoActionDialog.ITakePhotoActionDialogCallback {
    val singleResult = photoPickerForResult().also { it.setNeedLubanCompress(100) }

    val multiResult = compatMultiPhotoPickerForResult(3)

    val cameraHelper = CameraPermissionHelp(this, object : CameraPermissionHelp.Supplier {
        override fun createFileProvider(): Pair<File, Uri> {
            return createFileProviderMine()
        }
    })

    val cameraAndSelectHelper = CameraAndSelectPhotosPermissionHelper(this, object : CameraPermissionHelp.Supplier {
        override fun createFileProvider(): Pair<File, Uri> {
            return createFileProviderMine()
        }
    })

    private fun createFileProviderMine(): Pair<File, Uri> {
        val picture = File(Globals.goodCacheDir.path + "/shared")
        picture.mkdirs()
        val file = File(picture, "pic_" + System.currentTimeMillis() + ".jpg")
        val uri = FileProvider.getUriForFile(Globals.app, "${BuildConfig.APPLICATION_ID}.provider", file)
        return file to uri
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.takeDialog3.onClick {
            cameraAndSelectHelper.showPhotoAndCameraDialog(3)
        }
        binding.takeDialog1.onClick {
            cameraAndSelectHelper.showPhotoAndCameraDialog(1)
        }

        binding.takePic.onClick {
            cameraHelper.safeRunTakePic({result, createdTmpFile->
                if (result) {
                    LubanCompress().setResultCallback { srcPath, resultPath, isSuc ->
                        val r = if(isSuc) resultPath else srcPath
                        ignoreError {
                            val resultFile = File(r)
                            val resultUri = resultFile.toUri()
                            val cvtUri = UriHelper(resultUri, Globals.app.contentResolver).imageFileConvertToUriWrap()
                            logd { "cvtUri $cvtUri" }
                            showPic(cvtUri)
                        }
                    }.compress(requireContext(), createdTmpFile.toUri()) //必须是file的scheme。那个FileProvider提供的则不行。
                }
            })
        }

        binding.singlePic.onClick {
            singleResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE, null) { uri->
                logd { "uri: $uri" }
                showPic(uri)
            }
        }
        binding.singleVideo.onClick {
            singleResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.VIDEO, null) { uri->
                logd { "uri: $uri" }
                showPic(uri)
            }
        }
        binding.singlePicAndVideo.onClick {
            singleResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO, null) { uri->
                logd { "uri: $uri" }
                showPic(uri)
            }
        }
        binding.multiPic4.onClick {
            multiResult.setCurrentMaxItems(6)
            multiResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE, null) {uri->
                logd { "uri: $uri" }
                showPic(uri)
            }
        }
        binding.multiVideo3.onClick {
            multiResult.setCurrentMaxItems(3)
            multiResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.VIDEO, null) {uri->
                logd { "uri: $uri" }
                showPic(uri)
            }
        }
        binding.multiPicAndVideo5.onClick {
            multiResult.setCurrentMaxItems(9)
            multiResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO, null) {uri->
                logd { "uri: $uri" }
                showPic(uri)
            }
        }

        binding.multiPicV2.onClick {
            multiResult.setCurrentMaxItems(6)
            multiResult.launchByAll(MultiPhotoPickerContractResult.PickerType.IMAGE, null) {uris->
                for (uri in uris) {
                    logd { "uri: $uri" }
                    showPic(uri)
                }
            }
        }
        binding.multiVideoV2.onClick {
            multiResult.setCurrentMaxItems(3)
            multiResult.launchByAll(MultiPhotoPickerContractResult.PickerType.VIDEO, null) {uris->
                for (uri in uris) {
                    logd { "uri: $uri" }
                    showPic(uri)
                }
            }
        }
        binding.multiPicAndVideoV2.onClick {
            multiResult.setCurrentMaxItems(9)
            multiResult.launchByAll(MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO, null) {uris->
                for (uri in uris) {
                    logd { "uri: $uri" }
                    showPic(uri)
                }
            }
        }
    }

    var currentIndex = 1
    @Synchronized
    private fun showPic(uriWrap: UriWrap) {
        val pic = when(currentIndex) {
            1 -> binding.pic1
            2 -> binding.pic2
            3 -> binding.pic3
            4 -> binding.pic4
            5 -> binding.pic5
            else -> null
        }
        currentIndex++
        if (currentIndex == 6) {
            currentIndex = 1
        }
        pic?.glideSetAny(uriWrap.uri)
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
                        logd { "cvtUri $cvtUri" }
                        showPic(cvtUri)
                    }
                }.compress(requireContext(), createdTmpFile.toUri()) //必须是file的scheme。那个FileProvider提供的则不行。
            }
        })
    }

    override fun onClickSelectPhoto() {
        cameraAndSelectHelper.multiResult.launchByAll(MultiPhotoPickerContractResult.PickerType.IMAGE, null) {uris->
            for (uri in uris) {
                logd { "uri: $uri" }
                showPic(uri)
            }
        }
    }

    override fun onNothingClosed() {
    }
}