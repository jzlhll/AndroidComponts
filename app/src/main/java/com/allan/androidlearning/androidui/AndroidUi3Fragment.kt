package com.allan.androidlearning.androidui

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentAndroidUi3Binding
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_androidui.widget.CustomTextInputLayout

class AndroidUi3Fragment : BindingFragment<FragmentAndroidUi3Binding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.inputLayoutEmail.checkInputFun = {
            CustomTextInputLayout.matcherEmail(it)
        }

        binding.inputLayoutPassword.checkInputFun = {
            CustomTextInputLayout.matcherPassword(it)
        }
    }
}