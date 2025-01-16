package com.allan.androidlearning.activities

import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentFontTestBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingFragment

@EntryFrgName
class FontTestFragment : BindingFragment<FragmentFontTestBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.jumpBtn.onClick {
            FragmentRootActivity.start(requireActivity(), ActivityJumpFragment::class.java,
                enterAnim = com.au.module_android.R.anim.dialog_bottom_in_p,
                exitAnim = com.au.module_android.R.anim.dialog_bottom_out_p)
        }
    }
}