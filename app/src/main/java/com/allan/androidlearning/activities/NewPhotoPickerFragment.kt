package com.allan.androidlearning.activities

import android.os.Bundle
import androidx.activity.result.ActivityResultCallback
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import com.allan.androidlearning.BuildConfig
import com.allan.androidlearning.databinding.FragmentPhotoPickerBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.ignoreError
import com.au.module_android.utils.logd
import com.au.module_android.utilsmedia.UriHelper
import com.au.module_imagecompressed.CameraPermissionHelp
import com.au.module_imagecompressed.LubanCompress
import com.au.module_imagecompressed.MultiPhotoPickerContractResult
import com.au.module_imagecompressed.compatMultiPhotoPickerForResult
import com.au.module_imagecompressed.imageFileConvertToUriWrap
import com.au.module_imagecompressed.photoPickerForResult
import java.io.File

@EntryFrgName
class NewPhotoPickerFragment : BindingFragment<FragmentPhotoPickerBinding>() {
    val singleResult = photoPickerForResult().also { it.setNeedLubanCompress(100) }

    val multiResult = compatMultiPhotoPickerForResult(3)

    val cameraHelper = CameraPermissionHelp(this)

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.takePic.onClick {
            cameraHelper.safeRunTakePic(
                offerBlock = {
                    val picture = File(Globals.goodCacheDir.path)
                    picture.mkdirs()
                    val file = File(picture, "pic_" + System.currentTimeMillis() + ".jpg")
                    val uri = FileProvider.getUriForFile(
                        Globals.app,
                        "${BuildConfig.APPLICATION_ID}.provider", //根据file_path和androidManifest.xml而来
                        file
                    )
                    Pair(uri, object:ActivityResultCallback<Boolean> {
                        override fun onActivityResult(result: Boolean) {
                            if (result) {
                                LubanCompress().setResultCallback { srcPath, resultPath, isSuc ->
                                    val r = if(isSuc) resultPath else srcPath
                                    ignoreError {
                                        val resultFile = File(r)
                                        val resultUri = resultFile.toUri()
                                        val cvtUri = UriHelper(resultUri, Globals.app.contentResolver).imageFileConvertToUriWrap()
                                        logd { "cvtUri $cvtUri" }
                                    }
                                }.compress(requireContext(), file.toUri()) //必须是file的scheme。那个FileProvider提供的则不行。
                            }
                        }
                    })
                }
            )
        }

        binding.singlePic.onClick {
            singleResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE, null) { uri->
                logd { "uri: $uri" }
            }
        }
        binding.singleVideo.onClick {
            singleResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.VIDEO, null) {
                logd { "uri: $it" }
            }
        }
        binding.singlePicAndVideo.onClick {
            singleResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO, null) { uri->
                logd { "uri: $uri" }
            }
        }

        binding.multiPic4.onClick {
            multiResult.setCurrentMaxItems(6)
            multiResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE, null) {uri->
                logd { "uri: $uri" }
            }
        }
        binding.multiVideo3.onClick {
            multiResult.setCurrentMaxItems(3)
            multiResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.VIDEO, null) {uri->
                logd { "uri: $uri" }
            }
        }
        binding.multiPicAndVideo5.onClick {
            multiResult.setCurrentMaxItems(9)
            multiResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE_AND_VIDEO, null) {uri->
                logd { "uri: $uri" }
            }
        }
    }
}