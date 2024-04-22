package com.allan.autoclickfloat.activities.autooneclick

import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import com.allan.autoclickfloat.AllPermissionActivity
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.databinding.AutoContinuousClickAPointBinding
import com.allan.autoclickfloat.floats.WindowMgr
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.visible
import com.au.module_androiduilight.dialogs.ConfirmCenterDialog

class AutoContinuousClickActivityFragment : BindingFragment<AutoContinuousClickAPointBinding>() {

    private fun setupClickView() : SetupClickFloatView{
        var setupClickView = WindowMgr.findFloatView(key_auto_continuous_click).asOrNull<SetupClickFloatView>()
        if (setupClickView == null) {
            setupClickView = SetupClickFloatView()
        }
        return setupClickView
    }

    private fun showOrUpdateFloat() {
        val pair = Const.autoOnePoint.autoOnePointLiveData.value //不做一直监听
        val v = setupClickView()
        if (v.isShown) {
            v.updateViewPosition()
        } else {
            if (pair != null) {
                v.show(pair.first, pair.second)
            } else {
                v.show(100, 400)
            }
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        transparentStatusBar(this, true, true) {insets, statusBarsHeight, navigationBarHeight ->
            binding.root.setPadding(binding.root.paddingStart, statusBarsHeight, binding.root.paddingEnd, navigationBarHeight)
            insets
        }

        binding.showFloatViewBtn.onClick {
            showOrUpdateFloat()
        }
        binding.closeFloatViewBtn.onClick {
            WindowMgr.findFloatView(key_auto_continuous_click)?.remove()
        }

        binding.startAutoClickBtn.onClick {
            if (Const.autoOnePoint.autoOnePointLiveData.value != null) {
                Const.autoOnePoint.autoOnePointOpenLiveData.value = true
            }
        }
        binding.stopAutoClickBtn.onClick {
            Const.autoOnePoint.autoOnePointOpenLiveData.value = false
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
            binding.saveText.text = "已经保存的点：\n($x, $y)"
        }

        Const.autoOnePoint.autoOnePointOpenLiveData.observe(viewLifecycleOwner) {
            if (it) {
                binding.stopAutoClickBtn.visible()
                binding.startAutoClickBtn.gone()
                binding.inputMs.gone()
                binding.msText.gone()
                binding.showFloatViewBtn.gone()
                binding.closeFloatViewBtn.gone()
                showOrUpdateFloat()
                val v = setupClickView()
                v.icon.imageTintList = v.workingColor
                v.disableTouch = true
            } else {
                binding.stopAutoClickBtn.gone()
                binding.startAutoClickBtn.visible()
                binding.inputMs.visible()
                binding.msText.visible()
                binding.showFloatViewBtn.visible()
                binding.closeFloatViewBtn.visible()

                val v = setupClickView()
                v.icon.imageTintList = v.settingColor
                v.disableTouch = false
                v.remove()
            }
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
                    Const.autoOnePoint.autoOnePointOpenLiveData.value = false
                    it.dismissAllowingStateLoss()
                },
                cancelBlock = {
                    requireActivity().finishAfterTransition()
                })
            dialog.isCancelable = false
        }
    }

}