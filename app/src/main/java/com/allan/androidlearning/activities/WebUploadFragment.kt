package com.allan.androidlearning.activities

import android.os.Bundle
import com.allan.androidlearning.databinding.ActivityJsHtmlBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.ui.bindings.BindingFragment

/**
 * @author allan
 * @date :2024/12/5 9:40
 * @description:
 */
@EntryFrgName
class WebUploadFragment : BindingFragment<ActivityJsHtmlBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.webView.loadUrl("file:///android_asset/webupload/upload.html")
        binding.webView.setSelectPictureAction {

        }
    }
}