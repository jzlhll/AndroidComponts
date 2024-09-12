package com.allan.androidlearning.androidui

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentAndroidUi3Binding
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingFragment

class AndroidUi3Fragment : BindingFragment<FragmentAndroidUi3Binding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.gotoEditBtn.onClick {
            FragmentRootActivity.start(requireContext(), AndroidUiEditFragment::class.java)
        }
        binding.gotoEdit2Btn.onClick {
            FragmentRootActivity.start(requireContext(), AndroidUiEdit2Fragment::class.java)
        }
        binding.gotoEdit3Btn.onClick {
            FragmentRootActivity.start(requireContext(), AndroidUiEdit3Fragment::class.java)
        }
    }
}