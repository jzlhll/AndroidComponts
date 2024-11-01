package com.allan.androidlearning.activities

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentPhotoPickerBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd
import com.au.module_imagecompressed.MultiPhotoPickerContractResult
import com.au.module_imagecompressed.compatMultiPhotoPickerForResult
import com.au.module_imagecompressed.photoPickerForResult

@EntryFrgName
class NewPhotoPickerFragment : BindingFragment<FragmentPhotoPickerBinding>() {
    val singleResult = photoPickerForResult().also { it.setNeedLubanCompress(100) }

    val multiResult = compatMultiPhotoPickerForResult(3)

    override fun onBindingCreated(savedInstanceState: Bundle?) {
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