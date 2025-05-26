package com.allan.androidlearning.androidui

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentAndroidUi4Binding
import com.au.module_android.ui.bindings.BindingFragment

class AndroidUi4Fragment : BindingFragment<FragmentAndroidUi4Binding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        //设置初始化值
        binding.sizeView2.setRange(40, 100)
    }
}