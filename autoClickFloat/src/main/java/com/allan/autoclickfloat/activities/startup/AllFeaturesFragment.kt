package com.allan.autoclickfloat.activities.startup

import android.os.Bundle
import android.view.View
import com.allan.autoclickfloat.activities.autofs.AutoStartFragment
import com.allan.autoclickfloat.activities.autofs.canWrite
import com.allan.autoclickfloat.activities.autofs.goToManageSetting
import com.allan.autoclickfloat.activities.autooneclick.AutoContinuousClickActivityFragment
import com.allan.autoclickfloat.activities.nongyao.NongyaoFragment
import com.allan.autoclickfloat.activities.recordprojects.RecordProjectsAllFragment
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.databinding.AllFeaturesFragmentBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentRootActivity
import com.au.module_android.ui.FragmentRootOrientationActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.openApp
import com.au.module_androidui.dialogs.ConfirmCenterDialog

class AllFeaturesFragment : BindingFragment<AllFeaturesFragmentBinding>() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.autoClickButton.onClick {
            FragmentRootOrientationActivity.start(requireActivity(), AutoContinuousClickActivityFragment::class.java)
        }

        binding.recordModeBtn.onClick {
            FragmentRootOrientationActivity.start(requireActivity(), RecordProjectsAllFragment::class.java)
        }

        binding.nongyaoBtn.onClick {
            FragmentRootOrientationActivity.start(requireActivity(), NongyaoFragment::class.java)
        }

        binding.debugBtn.onClick {
            openApp(Globals.app, "com.tencent.tgclub")
        }

        Const.autoOnePoint.autoOnePointOpenLiveData.observe(viewLifecycleOwner) {
            if (it) {
                binding.autoClickButton.text = "自动点击（开启中）"
            } else {
                binding.autoClickButton.text = "自动点击"
            }
        }

        binding.autoFsBtn.onClick {
            if (canWrite(requireContext())) {
                FragmentRootActivity.start(requireContext(), AutoStartFragment::class.java)
            } else {
                ConfirmCenterDialog.show(childFragmentManager, "设置", "本功能需要调节亮度，即将跳转到系统设置，给予系统授权。", "OK", "取消",
                    cancelBlock = {
                    }, sureClick = {
                        goToManageSetting(requireActivity())
                        it.dismiss()
                    })
            }
        }
    }
}