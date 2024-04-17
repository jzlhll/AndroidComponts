package com.allan.autoclickfloat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.allan.autoclickfloat.databinding.NongyaoActivityBinding
import com.allan.autoclickfloat.floats.AutoClickService
import com.allan.autoclickfloat.floats.FloatingManager
import com.allan.autoclickfloat.floats.bean.ACTION_SHOW
import com.allan.autoclickfloat.floats.bean.ACTION_STOP
import com.allan.autoclickfloat.floats.bean.AutoClickInfo
import com.au.module_android.click.onClick
import com.au.module_android.permissions.gotoAccessibilityPermission
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_android.utils.gone
import com.au.module_android.utils.hideImeNew
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible

class AutoClickActivity : AppCompatActivity() {
    private lateinit var mBinding:NongyaoActivityBinding

    private val twoPermissionLost = "先打开无障碍权限 和 悬浮窗顶层权限"
    private val accessibilityPermissionLost = "先打开无障碍权限"
    private val floatWindowPermissionLost = "先打开悬浮窗顶层权限"

    private val viewModel by unsafeLazy { ViewModelProvider(this)[AutoClickViewModel::class.java] }

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
        startAutoClickService()
    }

    private fun startAutoClickService() {
        val intent = Intent(this, AutoClickService::class.java)
        startForegroundService(intent)
    }

    private fun stopAutoClickService() {
        val intent = Intent(this, AutoClickService::class.java).also {
            intent.putExtra("myAction", "stopService")
        }
        startForegroundService(intent)
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPermission(this)
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
            FloatingManager.autoClickLiveData.setValueSafe(AutoClickInfo(ACTION_SHOW))
        }

        mBinding.closeFloatViewBtn.onClick {
            FloatingManager.autoClickLiveData.setValueSafe(AutoClickInfo(ACTION_STOP))
            stopAutoClickService()
        }

        mBinding.startAutoClickBtn.onClick {
            hideImeNew(window, mBinding.intervalEdit)
        }
    }
}