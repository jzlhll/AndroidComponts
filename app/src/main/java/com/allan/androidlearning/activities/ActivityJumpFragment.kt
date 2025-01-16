package com.allan.androidlearning.activities

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentJumpBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment

@EntryFrgName
class ActivityJumpFragment : BindingFragment<FragmentJumpBinding>() {

    override fun onBindingCreated(savedInstanceState: Bundle?) {
         binding.backBtn.onClick {
             requireActivity().finish()
         }
    }
}