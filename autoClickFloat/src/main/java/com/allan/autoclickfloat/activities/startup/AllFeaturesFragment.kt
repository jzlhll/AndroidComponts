package com.allan.autoclickfloat.activities.startup

import android.os.Bundle
import android.view.View
import com.allan.autoclickfloat.GlobalsAccessService
import com.allan.autoclickfloat.activities.autooneclick.AutoContinuousClickActivityFragment
import com.allan.autoclickfloat.activities.recordprojects.RecordProjectsAllFragment
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.databinding.AllFeaturesFragmentBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.FragmentRootOrientationActivity
import com.au.module_android.ui.bindings.BindingFragment

class AllFeaturesFragment : BindingFragment<AllFeaturesFragmentBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.autoClickButton.onClick {
            FragmentRootOrientationActivity.start(requireActivity(), AutoContinuousClickActivityFragment::class.java)
        }

        binding.recordModeBtn.onClick {
            FragmentRootOrientationActivity.start(requireActivity(), RecordProjectsAllFragment::class.java)
        }

        Const.autoOnePoint.autoOnePointOpenLiveData.observe(viewLifecycleOwner) {
            if (it) {
                binding.autoClickButton.text = "自动点击（开启中）"
            } else {
                binding.autoClickButton.text = "自动点击"
            }
        }
    }
}