package com.allan.autoclickfloat.activities.autooneclick

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.allan.autoclickfloat.AllPermissionActivity
import com.allan.autoclickfloat.activities.autooneclick.SetupClickFloatView
import com.allan.autoclickfloat.databinding.AutoContinuousClickAPointBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.transparentStatusBar
import kotlinx.coroutines.launch

class AutoContinuousClickActivityFragment : BindingFragment<AutoContinuousClickAPointBinding>() {
    private var setupClickView: SetupClickFloatView? = null

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        transparentStatusBar(this, true, true) {insets, statusBarsHeight, navigationBarHeight ->
            binding.root.setPadding(binding.root.paddingStart, statusBarsHeight, binding.root.paddingEnd, navigationBarHeight)
            insets
        }

        binding.showFloatViewBtn.onClick {
            if (setupClickView == null) {
                setupClickView = SetupClickFloatView().also {
                    it.callback = {x, y->
                        Log.d("allan", "autoClick x=$x, y=$y")
                        lifecycleScope.launch {
                            binding.saveText.text = "已经保存的点：\n($x, $y)"
                        }
                    }
                }
            }

            lifecycleScope.launch {
                setupClickView?.showInit()
            }
        }
        binding.closeFloatViewBtn.onClick {
            setupClickView?.remove()
        }

        binding.startAutoClickBtn.onClick {

        }
    }

    override fun onResume() {
        super.onResume()
        AllPermissionActivity.checkGotoAllPermissionActivity(this)
    }

}