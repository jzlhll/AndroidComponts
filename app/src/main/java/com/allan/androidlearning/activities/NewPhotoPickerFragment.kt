package com.allan.androidlearning.activities

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.allan.androidlearning.databinding.FragmentPhotoPickerBinding
import com.allan.classnameanno.EntroFrgName
import com.au.module.imagecompressed.LubanCompress
import com.au.module_android.click.onClick
import com.au.module_android.permissions.multiPhotoPickerForResult
import com.au.module_android.permissions.photoPickerForResult
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd

@EntroFrgName
class NewPhotoPickerFragment : BindingFragment<FragmentPhotoPickerBinding>() {
    val singlePicResult = photoPickerForResult(ActivityResultContracts.PickVisualMedia.ImageOnly)
    val singleVideoResult = photoPickerForResult(ActivityResultContracts.PickVisualMedia.VideoOnly)
    val singlePicAndVideoResult = photoPickerForResult(ActivityResultContracts.PickVisualMedia.ImageAndVideo)

    val multiPicResult = multiPhotoPickerForResult(4, ActivityResultContracts.PickVisualMedia.ImageOnly)
    val multiVideoResult = multiPhotoPickerForResult(3, ActivityResultContracts.PickVisualMedia.VideoOnly)
    val multiPicAndVideoResult = multiPhotoPickerForResult(5, ActivityResultContracts.PickVisualMedia.ImageAndVideo)

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.singlePic.onClick {
            singlePicResult.request { uri->
                logd { "uri: $uri" }
                LubanCompress().also {
                    it.resultCallback = { srcPath, resultPath ->
                        logd { "srcPath: $srcPath, resultPath $resultPath" }
                    }
                    it.loadSourceBlock = { builder ->
                        builder.load(uri)
                    }
                }.compress(requireContext())
            }
        }
        binding.singleVideo.onClick {
            singleVideoResult.request {
                logd { "uri: $it" }
            }
        }
        binding.singlePicAndVideo.onClick {
            singlePicAndVideoResult.request{ uri->
                logd { "uri: $uri" }
                LubanCompress().also {
                    it.resultCallback = { srcPath, resultPath ->
                        logd { "srcPath: $srcPath, resultPath $resultPath" }
                    }
                    it.loadSourceBlock = { builder ->
                        builder.load(uri)
                    }
                }.compress(requireContext())
            }
        }

        binding.multiPic4.onClick {
            multiPicResult.request {
                it.forEachIndexed { index, uri ->
                    logd { "$index, uri: $uri" }
                    LubanCompress().also { lc->
                        lc.resultCallback = { srcPath, resultPath ->
                            logd { "srcPath: $srcPath, resultPath $resultPath" }
                        }
                        lc.loadSourceBlock = { builder ->
                            builder.load(uri)
                        }
                    }.compress(requireContext())
                }
            }
        }
        binding.multiVideo3.onClick {
            multiVideoResult.request {
                it.forEachIndexed { index, uri ->
                    logd { "$index, uri: $uri" }
                }
            }
        }
        binding.multiPicAndVideo5.onClick {
            multiPicAndVideoResult.request {
                it.forEachIndexed { index, uri ->
                    logd { "$index, uri: $uri" }
                    LubanCompress().also { lc->
                        lc.resultCallback = { srcPath, resultPath ->
                            logd { "srcPath: $srcPath, resultPath $resultPath" }
                        }
                        lc.loadSourceBlock = { builder ->
                            builder.load(uri)
                        }
                    }.compress(requireContext())
                }
            }
        }
    }
}