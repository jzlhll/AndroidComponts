package com.au.jobstudy

import android.os.Bundle
import com.au.jobstudy.databinding.FragmentMainMineBinding
import com.au.module_android.ui.bindings.BindingFragment

class MainMineFragment : BindingFragment<FragmentMainMineBinding>() {
    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentMainMineBinding) {
        viewBinding.logoText.text = getString(R.string.app_name) + " ${AndroidSdkMapping().currentVersionStr}"
    }
}