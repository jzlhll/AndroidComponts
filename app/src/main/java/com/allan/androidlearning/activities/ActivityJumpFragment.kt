package com.allan.androidlearning.activities

import android.app.Activity
import android.os.Build
import android.os.Bundle
import com.allan.androidlearning.databinding.FragmentJumpBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment

@EntryFrgName
class ActivityJumpFragment : BindingFragment<FragmentJumpBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requireActivity().overrideActivityTransition(Activity.OVERRIDE_TRANSITION_OPEN, com.au.module_android.R.anim.dialog_bottom_in_p, 0)
            requireActivity().overrideActivityTransition(Activity.OVERRIDE_TRANSITION_CLOSE, 0, com.au.module_android.R.anim.dialog_bottom_out_p)
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            requireActivity().overridePendingTransition(com.au.module_android.R.anim.dialog_bottom_in_p, 0)
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
         binding.backBtn.onClick {
             requireActivity().finish()
             if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                 requireActivity().overridePendingTransition(0, com.au.module_android.R.anim.dialog_bottom_out_p)
             }
         }
    }

    override val afterFinish: (() -> Unit)?
        get() = {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                requireActivity().overridePendingTransition(0, com.au.module_android.R.anim.dialog_bottom_out_p)
            }
        }
}