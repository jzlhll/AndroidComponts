package com.allan.androidlearning.activities

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentMonospaceTextBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.ui.bindings.BindingFragment

/**
 * @author allan
 * @date :2024/5/24 9:48
 * @description:
 */
@EntryFrgName
class MonoSpaceFragment : BindingFragment<FragmentMonospaceTextBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.firstMonoText.setTimeString("05:33 / 78:59")
        binding.secondMonoText.setTimeString("12:34 / 56:09")
        binding.thirdMonoText.setTimeString("12:34 / 56:09")
    }
}