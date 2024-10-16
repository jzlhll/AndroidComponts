package com.allan.androidlearning.activities

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.allan.androidlearning.databinding.FragmentCropEntryBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_imagecompressed.CropCircleImageFragment
import com.au.module_android.click.onClick
import com.au.module_android.glide.glideSetAnyAsCircleCrop
import com.au.module_android.permissions.createActivityForResult
import com.au.module_android.permissions.photoPickerForResult
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.iteratorPrint
import com.au.module_android.utils.logd
import java.io.File

/**
 * @author allan
 * @date :2024/10/15 11:28
 * @description:
 */
@EntryFrgName
class CropEntryFragment : BindingFragment<FragmentCropEntryBinding>() {
    private val gotoUcropResult = createActivityForResult()
    private val photoPickResult = photoPickerForResult(ActivityResultContracts.PickVisualMedia.ImageOnly)

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.button.onClick {
            photoPickResult.request { uri ->
                logd { "uri $uri" }
                if (uri != null) {
                    CropCircleImageFragment.startCropForResult(requireContext(), gotoUcropResult, uri) {
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
                                binding.avatarImage.setBackgroundDrawable(null)
                                binding.avatarImage.glideSetAnyAsCircleCrop(file)
                            }
                        }
                    }
                }
            }
        }
    }
}