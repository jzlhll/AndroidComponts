package com.au.jobstudy

import android.os.Bundle
import com.au.jobstudy.databinding.FragmentMainHomeBinding
import com.au.module_android.ui.bindings.BindingFragment

class MainHomeFragment : BindingFragment<FragmentMainHomeBinding>() {
    override fun afterViewCreated(savedInstanceState: Bundle?, viewBinding: FragmentMainHomeBinding) {
        viewBinding.checkPointList
    }
}