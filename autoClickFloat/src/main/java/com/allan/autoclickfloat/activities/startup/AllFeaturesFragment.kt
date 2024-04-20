package com.allan.autoclickfloat.activities.startup

import android.os.Bundle
import android.view.View
import com.allan.autoclickfloat.activities.autooneclick.AutoContinuousClickActivityFragment
import com.allan.autoclickfloat.databinding.AllFeaturesFragmentBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.bindings.BindingFragment

class AllFeaturesFragment : BindingFragment<AllFeaturesFragmentBinding>() {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.autoClickButton.onClick {
            FragmentRootActivity.start(requireActivity(), AutoContinuousClickActivityFragment::class.java)
        }

        binding.recordModeBtn.onClick {
          //  FragmentRootActivity.start(requireActivity(), RecordModeActivityFragment::class.java)
        }

    }
}