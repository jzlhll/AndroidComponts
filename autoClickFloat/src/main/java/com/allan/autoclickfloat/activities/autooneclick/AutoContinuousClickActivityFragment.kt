package com.allan.autoclickfloat.activities.autooneclick

import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import com.allan.autoclickfloat.AllPermissionActivity
import com.allan.autoclickfloat.activities.startup.PermissionsHelper
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.databinding.AutoContinuousClickAPointBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmCenterDialog

class AutoContinuousClickActivityFragment : BindingFragment<AutoContinuousClickAPointBinding>() {

    private fun setupClickView() = SetupClickFloatView.getInstance()

    private fun showOrUpdateFloat() {
        val info = Const.autoOnePoint.autoOnePointLiveData.value //不做一直监听
        val v = setupClickView()
        if (!v.isShown) {
            if (info != null) {
                v.show(info.first, info.second, info.third ?: Const.rotationLiveData.value!!)
            } else {
                v.show(100, 400, Const.rotationLiveData.value!!)
            }
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.showFloatViewBtn.onClick {
            showOrUpdateFloat()
        }
        binding.closeFloatViewBtn.onClick {
            SetupClickFloatView.getInstanceOrNull()?.remove()
        }

        binding.startAutoClickBtn.onClick {
            if (PermissionsHelper.showGotoSystemAccessibilityPermission(this)) {
                if (Const.autoOnePoint.autoOnePointLiveData.value != null) {
                    Const.autoOnePoint.autoOnePointOpenLiveData.setValueSafe(true)
                }
            }
        }
        binding.stopAutoClickBtn.onClick {
            Const.autoOnePoint.autoOnePointOpenLiveData.setValueSafe(false)
        }

        binding.inputMs.doAfterTextChanged {
            val s = it.toString()
            if (s.isNotEmpty()) {
                var ms = it.toString().toInt()
                if (ms < 250) {
                    ms = 250
                }
                Const.autoOnePoint.saveAutoOnePointMs(ms)
            }
        }

        binding.backBtn.onClick {
            requireActivity().finishAfterTransition()
        }

        val ms = Const.autoOnePoint.autoOnePointClickMsLiveData.value //不做一直的监听。不然就来回设置input了
        binding.inputMs.setText(ms.toString())

        Const.autoOnePoint.autoOnePointLocLiveData.observe(viewLifecycleOwner) {
            val x = it.first
            val y = it.second
            if (checkScreenRotationIsPort(resources)) {
                binding.saveText.text = "已经保存的点：\n($x, $y)"
            } else {
                binding.saveText.text = "已经保存的点：($x, $y)"
            }
        }

        changeWhenOpenChange(Const.autoOnePoint.autoOnePointOpenLiveData.value!!)
        Const.autoOnePoint.autoOnePointOpenLiveData.observeUnStick(viewLifecycleOwner) {
            it!!
            changeWhenOpenChange(it)
            if (it) {
                showOrUpdateFloat()
                val v = setupClickView()
                v.icon.imageTintList = v.workingColor
                v.disableTouch = true
            } else {
                val v = setupClickView()
                v.icon.imageTintList = v.settingColor
                v.disableTouch = false
                v.remove()
            }
        }
    }

    private fun changeWhenOpenChange(it:Boolean) {
        if (it) {
            binding.stopAutoClickBtn.visible()
            binding.startAutoClickBtn.gone()
            binding.inputMs.gone()
            binding.msText.gone()
            binding.showFloatViewBtn.gone()
            binding.closeFloatViewBtn.gone()
        } else {
            binding.stopAutoClickBtn.gone()
            binding.startAutoClickBtn.visible()
            binding.inputMs.visible()
            binding.msText.visible()
            binding.showFloatViewBtn.visible()
            binding.closeFloatViewBtn.visible()
        }
    }

    override fun onResume() {
        super.onResume()
        AllPermissionActivity.checkGotoAllPermissionActivity(this)

        if (Const.autoOnePoint.autoOnePointOpenLiveData.value == true) {
            val dialog = ConfirmCenterDialog.show(childFragmentManager,
                "如果需要设置的话，是否关闭？",
                "",
                "关闭",
                "退出",
                sureClick = {
                    Const.autoOnePoint.autoOnePointOpenLiveData.setValueSafe(false)
                    it.dismissAllowingStateLoss()
                },
                cancelBlock = {
                    requireActivity().finishAfterTransition()
                })
            dialog.isCancelable = false
        }
    }

}