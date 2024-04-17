package com.allan.autoclickfloat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.allan.autoclickfloat.databinding.NongyaoActivityBinding
import com.allan.autoclickfloat.databinding.SetupOneProjectFragmentBinding
import com.allan.autoclickfloat.floats.AutoClickService.Companion.startAutoClickService
import com.allan.autoclickfloat.floats.AutoClickService.Companion.stopAutoClickService
import com.allan.autoclickfloat.floats.FloatingManager
import com.allan.autoclickfloat.floats.bean.ACTION_REMOVE
import com.allan.autoclickfloat.floats.bean.ACTION_SHOW
import com.allan.autoclickfloat.floats.bean.AutoClickInfo
import com.au.module_android.click.onClick
import com.au.module_android.permissions.gotoAccessibilityPermission
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.utils.gone
import com.au.module_android.utils.hideImeNew
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible

class AutoClickActivity : BindingFragment<SetupOneProjectFragmentBinding>() {

    private fun initData() {
        viewModel.allPermissionEnabled.observeUnStick(this) {
            when (it) {
                AutoClickViewModel.STATE_ALL_NO_PERMISSION -> {
                    mBinding.requestPermissionsBtn.visible()
                    mBinding.permissionTv.visible()
                    mBinding.permissionTv.text = twoPermissionLost

                    mBinding.startAutoClickBtn.gone()
                    mBinding.closeFloatViewBtn.gone()
                    mBinding.showFloatViewBtn.gone()
                }
                AutoClickViewModel.STATE_NO_FLOAT_WINDOW -> {
                    mBinding.requestPermissionsBtn.visible()
                    mBinding.permissionTv.visible()
                    mBinding.permissionTv.text = floatWindowPermissionLost

                    mBinding.startAutoClickBtn.gone()
                    mBinding.closeFloatViewBtn.gone()
                    mBinding.showFloatViewBtn.gone()
                }
                AutoClickViewModel.STATE_NO_ACCESSIBILITY -> {
                    mBinding.requestPermissionsBtn.visible()
                    mBinding.permissionTv.visible()
                    mBinding.permissionTv.text = accessibilityPermissionLost

                    mBinding.startAutoClickBtn.gone()
                    mBinding.closeFloatViewBtn.gone()
                    mBinding.showFloatViewBtn.gone()
                }
                AutoClickViewModel.STATE_ALL_PERMISSION_ENABLE -> {
                    mBinding.requestPermissionsBtn.gone()
                    mBinding.permissionTv.gone()

                    mBinding.startAutoClickBtn.visible()
                    mBinding.closeFloatViewBtn.visible()
                    mBinding.showFloatViewBtn.visible()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar(this, true, true) {insets, statusBarsHeight, navigationBarHeight ->
            mBinding.root.setPadding(0, statusBarsHeight, 0, navigationBarHeight)
            insets
        }
        val vb = NongyaoActivityBinding.inflate(layoutInflater)
        mBinding = vb
        setContentView(vb.root)

        initData()
        initListener()
    }


    override fun onResume() {
        super.onResume()
        if (AutoClickViewModel.isAccessibilityEnabled(this) && AutoClickViewModel.isFloatWindowEnabled(this)) {
        } else {
            StrongCenterDialog.show() {
                finish()
                startActivity(Intent(this, AutoClickActivity::class.java))
            }
        }
    }

    private fun initListener() {
        mBinding.requestPermissionsBtn.onClick {
            if (viewModel.allPermissionEnabled.value == AutoClickViewModel.STATE_ALL_NO_PERMISSION
                    || viewModel.allPermissionEnabled.value == AutoClickViewModel.STATE_NO_ACCESSIBILITY) {
                gotoAccessibilityPermission()
            } else if (viewModel.allPermissionEnabled.value == AutoClickViewModel.STATE_ALL_NO_PERMISSION
                || viewModel.allPermissionEnabled.value == AutoClickViewModel.STATE_NO_FLOAT_WINDOW) {
                gotoFloatWindowPermission()
            }
        }

        mBinding.showFloatViewBtn.onClick {
            startAutoClickService()
            FloatingManager.autoClickLiveData.setValueSafe(AutoClickInfo(ACTION_SHOW))
        }

        mBinding.closeFloatViewBtn.onClick {
            FloatingManager.autoClickLiveData.setValueSafe(AutoClickInfo(ACTION_REMOVE))
            stopAutoClickService()
        }

        mBinding.startAutoClickBtn.onClick {
            hideImeNew(window, mBinding.intervalEdit)
        }
    }
}