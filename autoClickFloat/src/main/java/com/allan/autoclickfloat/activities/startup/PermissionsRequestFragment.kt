package com.allan.autoclickfloat.activities.startup

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.allan.autoclickfloat.AllPermissionActivity
import com.allan.autoclickfloat.databinding.PermissionsFragmentBinding
import com.au.module_android.click.onClick
import com.au.module_android.permissions.gotoAccessibilityPermission
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible

class PermissionsRequestFragment : BindingFragment<PermissionsFragmentBinding>() {
    private val viewModel by unsafeLazy { ViewModelProvider(requireActivity())[PermissionsViewModel::class.java] }

    private val twoPermissionLost = "开启无障碍功能 和 悬浮窗权限"
    private val accessibilityPermissionLost = "开启无障碍功能"
    private val floatWindowPermissionLost = "开启悬浮窗权限"

    private fun initData() {
        viewModel.allPermissionEnabled.observe(viewLifecycleOwner) {
            when (it) {
                PermissionsViewModel.STATE_ALL_NO_PERMISSION -> {
                    binding.requestPermissionsBtn.visible()
                    binding.permissionTv.visible()
                    binding.permissionTv.text = twoPermissionLost
                    requireActivity().asOrNull<AllPermissionActivity>()?.showPermissionsRequest()
                }
                PermissionsViewModel.STATE_NO_FLOAT_WINDOW -> {
                    binding.requestPermissionsBtn.visible()
                    binding.permissionTv.visible()
                    binding.permissionTv.text = floatWindowPermissionLost
                    requireActivity().asOrNull<AllPermissionActivity>()?.showPermissionsRequest()
                }
                PermissionsViewModel.STATE_NO_ACCESSIBILITY -> {
                    binding.requestPermissionsBtn.visible()
                    binding.permissionTv.visible()
                    binding.permissionTv.text = accessibilityPermissionLost
                    requireActivity().asOrNull<AllPermissionActivity>()?.showPermissionsRequest()
                }
                PermissionsViewModel.STATE_ALL_PERMISSION_ENABLE -> {
                    requireActivity().asOrNull<AllPermissionActivity>()?.showAllFeatures()
                }
            }
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        initData()

        binding.requestPermissionsBtn.onClick {
            if (viewModel.allPermissionEnabled.value == PermissionsViewModel.STATE_ALL_NO_PERMISSION
                || viewModel.allPermissionEnabled.value == PermissionsViewModel.STATE_NO_ACCESSIBILITY
            ) {
                gotoAccessibilityPermission()
            } else if (viewModel.allPermissionEnabled.value == PermissionsViewModel.STATE_ALL_NO_PERMISSION
                || viewModel.allPermissionEnabled.value == PermissionsViewModel.STATE_NO_FLOAT_WINDOW
            ) {
                gotoFloatWindowPermission()
            }
        }
    }
}