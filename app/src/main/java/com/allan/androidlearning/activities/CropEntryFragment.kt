package com.allan.androidlearning.activities

import android.net.Uri
import android.os.Build
import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentCropEntryBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_nested.fragments.AbsNestedIndicatorFragment
import com.au.module_android.click.onClick
import com.au.module_android.glide.glideSetAnyAsCircleCrop
import com.au.module_android.permissions.createActivityForResult
import com.au.module_android.utils.iteratorPrint
import com.au.module_android.utils.logd
import com.au.module_imagecompressed.CropCircleImageFragment
import com.au.module_imagecompressed.MultiPhotoPickerContractResult
import com.au.module_imagecompressed.photoPickerForResult
import java.io.File

/**
 * @author allan
 * @date :2024/10/15 11:28
 * @description:
 */
@EntryFrgName
class CropEntryFragment : AbsNestedIndicatorFragment<Void, FragmentCropEntryBinding>() {
    private val gotoUcropResult = createActivityForResult()
    private val photoPickResult = photoPickerForResult()

    override fun isContentViewMergeXml(): Boolean {
        return true
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        contentViewBinding.button.onClick {
            photoPickResult.launchOneByOne(MultiPhotoPickerContractResult.PickerType.IMAGE, null) { uri ->
                logd { "uri $uri" }
                CropCircleImageFragment.startCropForResult(requireContext(), gotoUcropResult, uri.uri) {
                    val intent = it.data
                    val code = it.resultCode
                    logd { "code $code, data: " + intent }
                    intent?.iteratorPrint()
                    if (code == CropCircleImageFragment.RESULT_OK) {
                        val croppedUri:Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                intent?.getParcelableExtra(CropCircleImageFragment.RESULT_KEY_CROPPED_IMAGE, Uri::class.java)
                            } else {
                                intent?.getParcelableExtra(CropCircleImageFragment.RESULT_KEY_CROPPED_IMAGE)
                            }

                        logd { "croppedUri: " + croppedUri }
                        croppedUri?.let { cropped ->
                            val file = File(cropped.toString().replace("file://", ""))
                            logd { "croppedUri file: " + file + " size: " + file.length()}
                            contentViewBinding.avatarImage.setBackgroundDrawable(null)
                            contentViewBinding.avatarImage.glideSetAnyAsCircleCrop(file)
                        }
                    }
                }
            }
        }
    }
}