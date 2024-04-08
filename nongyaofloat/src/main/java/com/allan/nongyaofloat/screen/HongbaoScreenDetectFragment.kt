package com.allan.nongyaofloat.screen

import android.os.Bundle
import android.view.View
import com.allan.nongyaofloat.databinding.FragmentHongbaoScreenBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment

class HongbaoScreenDetectFragment : BindingFragment<FragmentHongbaoScreenBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.startScreenDetectBtn.onClick {

        }
    }
}