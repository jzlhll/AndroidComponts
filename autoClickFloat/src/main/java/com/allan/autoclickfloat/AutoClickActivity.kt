package com.allan.autoclickfloat

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.accessibility.AccessibilityManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.allan.autoclickfloat.databinding.NongyaoActivityBinding
import com.allan.autoclickfloat.floats.views.FloatingSettingView
import com.allan.autoclickfloat.floats.AutoClickService
import com.allan.autoclickfloat.floats.views.FloatingStepView
import com.allan.autoclickfloat.floats.views.WindowMgr
import com.au.module_android.click.onClick
import com.au.module_android.permissions.gotoAccessibilityPermission
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_android.permissions.hasFloatWindowPermission
import com.au.module_android.utils.gone
import com.au.module_android.utils.hideImeNew
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.visible

class AutoClickActivity : AppCompatActivity() {
    private lateinit var mBinding:NongyaoActivityBinding

    private val twoPermissionLost = "先打开无障碍权限 和 悬浮窗顶层权限"
    private val accessibilityPermissionLost = "先打开无障碍权限"
    private val floatWindowPermissionLost = "先打开悬浮窗顶层权限"

    private fun isAccessibilityEnabled() : Boolean {
        val accessibilityMgr = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        return accessibilityMgr.isEnabled
    }

    private fun isFloatWindowEnabled() = hasFloatWindowPermission()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transparentStatusBar(this, true, true) {insets, statusBarsHeight, navigationBarHeight ->
            mBinding.root.setPadding(0, statusBarsHeight, 0, navigationBarHeight)
            insets
        }
        val vb = NongyaoActivityBinding.inflate(layoutInflater)
        mBinding = vb
        setContentView(vb.root)

        initListener()
        startAutoClickService()
    }

    private fun startAutoClickService() {
        val intent = Intent(this, AutoClickService::class.java)
        startForegroundService(intent)
    }

    override fun onResume() {
        super.onResume()
        val acc = isAccessibilityEnabled()
        val fw = isFloatWindowEnabled()
        if (acc && fw) {
            mBinding.permissionTv.gone()
            mBinding.requestPermissionsBtn.gone()
        } else {
            mBinding.permissionTv.visible()
            mBinding.requestPermissionsBtn.visible()

            if (acc) {
                mBinding.permissionTv.text = floatWindowPermissionLost
            } else if (fw) {
                mBinding.permissionTv.text = accessibilityPermissionLost
            } else {
                mBinding.permissionTv.text = twoPermissionLost
            }
        }
    }

    private fun initListener() {
        mBinding.requestPermissionsBtn.onClick {
            if (!isAccessibilityEnabled()) {
                gotoAccessibilityPermission()
            } else if (isFloatWindowEnabled()) {
                gotoFloatWindowPermission()
            }
        }

        mBinding.showFloatViewBtn.onClick {

        }

        mBinding.closeFloatViewBtn.onClick {
        }

        mBinding.startAutoClickBtn.onClick {
            hideImeNew(window, mBinding.intervalEdit)
        }
    }
}