package com.allan.androidlearning.activities

import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import com.allan.androidlearning.databinding.FragmentCropEntryBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.permissions.createActivityForResult
import com.au.module_android.permissions.photoPickerForResult
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd

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
            }
        }
    }
}