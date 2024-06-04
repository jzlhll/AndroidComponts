package com.allan.autoclickfloat.activities.nongyao

import android.os.Bundle
import com.allan.autoclickfloat.activities.startup.PermissionsHelper.Companion.showGotoSystemAccessibilityPermission
import com.allan.autoclickfloat.activities.startup.PermissionsHelper.Companion.showGotoSystemFloatWindowPermission
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.databinding.FragmentNongyaoBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

/**
 * @author allan
 * @date :2024/6/4 16:38
 * @description:
 */
class NongyaoFragment : BindingFragment<FragmentNongyaoBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        Const.appClickTasks.openLiveData.observe(this) {
            if (it) {
                binding.stopAutoClickBtn.visible()
                binding.startAutoClickBtn.gone()
            } else {
                binding.stopAutoClickBtn.gone()
                binding.startAutoClickBtn.visible()
            }
        }

        binding.stopAutoClickBtn.onClick {
            Const.appClickTasks.openLiveData.setValueSafe(false)
        }

        binding.startAutoClickBtn.onClick {
            if (showGotoSystemFloatWindowPermission(this)) {
                if (showGotoSystemAccessibilityPermission(this)) {
                    Const.appClickTasks.openLiveData.setValueSafe(true)
                }
            }
        }
    }
}