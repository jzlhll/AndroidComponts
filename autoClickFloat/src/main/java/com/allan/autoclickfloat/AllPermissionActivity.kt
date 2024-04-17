package com.allan.autoclickfloat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.allan.autoclickfloat.databinding.PermissionsActivityBinding
import com.au.module_android.click.onClick
import com.au.module_android.permissions.gotoAccessibilityPermission
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible

/**
 * @author allan
 * @date :2024/4/17 11:43
 * @description:
 */
class AllPermissionActivity : AppCompatActivity() {
    private lateinit var mBinding: PermissionsActivityBinding

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

                }
                AutoClickViewModel.STATE_NO_FLOAT_WINDOW -> {
                    mBinding.requestPermissionsBtn.visible()
                    mBinding.permissionTv.visible()
                    mBinding.permissionTv.text = floatWindowPermissionLost

                }
                AutoClickViewModel.STATE_NO_ACCESSIBILITY -> {
                    mBinding.requestPermissionsBtn.visible()
                    mBinding.permissionTv.visible()
                    mBinding.permissionTv.text = accessibilityPermissionLost
                }
                AutoClickViewModel.STATE_ALL_PERMISSION_ENABLE -> {
                    finishAfterTransition()
                    startActivity(Intent(this, AutoClickActivity::class.java))
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (AutoClickViewModel.isAccessibilityEnabled(this) && AutoClickViewModel.isFloatWindowEnabled(this)) {
            finish()
            startActivity(Intent(this, AutoClickActivity::class.java))
        } else {
            transparentStatusBar(this, true, true) {insets, statusBarsHeight, navigationBarHeight ->
                mBinding.root.setPadding(0, statusBarsHeight, 0, navigationBarHeight)
                insets
            }
            val vb = PermissionsActivityBinding.inflate(layoutInflater)
            mBinding = vb
            setContentView(vb.root)

            initData()
            initListener()
        }
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
    }
}