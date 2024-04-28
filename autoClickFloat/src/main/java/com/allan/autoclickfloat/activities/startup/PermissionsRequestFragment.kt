package com.allan.autoclickfloat.activities.startup

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.allan.autoclickfloat.AllPermissionActivity
import com.allan.autoclickfloat.databinding.PermissionsFragmentBinding
import com.au.module_android.click.onClick
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible

class PermissionsRequestFragment : BindingFragment<PermissionsFragmentBinding>() {
    private val viewModel by unsafeLazy { ViewModelProvider(requireActivity())[OnlyFloatPermissionViewModel::class.java] }

    private val floatWindowPermissionLost = "开启悬浮窗权限"

    private fun initData() {
        viewModel.allPermissionEnabled.observe(viewLifecycleOwner) {
            when (it) {
                OnlyFloatPermissionViewModel.STATE_NO_FLOAT_WINDOW -> {
                    binding.requestPermissionsBtn.visible()
                    binding.permissionTv.visible()
                    binding.permissionTv.text = floatWindowPermissionLost
                    requireActivity().asOrNull<AllPermissionActivity>()?.showPermissionsRequest()
                }
                OnlyFloatPermissionViewModel.STATE_ALL_PERMISSION_ENABLE -> {
                    requireActivity().asOrNull<AllPermissionActivity>()?.showAllFeatures()
                }
            }
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        initData()

        binding.requestPermissionsBtn.onClick {
            gotoFloatWindowPermission()
        }
    }
}