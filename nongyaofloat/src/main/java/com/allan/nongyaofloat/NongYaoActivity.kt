package com.allan.nongyaofloat

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.accessibility.AccessibilityManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.allan.nongyaofloat.databinding.NongyaoActivityBinding
import com.allan.nongyaofloat.floats.views.FloatingSettingView
import com.allan.nongyaofloat.floats.NongYaoAutoClickService
import com.allan.nongyaofloat.floats.views.FloatingStepView
import com.allan.nongyaofloat.floats.views.WindowMgr
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.permissions.gotoAccessibilityPermission
import com.au.module_android.permissions.gotoFloatWindowPermission
import com.au.module_android.permissions.hasFloatWindowPermission
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

class NongYaoActivity : AppCompatActivity() {
    private lateinit var mBinding:NongyaoActivityBinding
    private val tag = "NongyaoActivity"

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
        enableEdgeToEdge()
        val vb = NongyaoActivityBinding.inflate(layoutInflater)
        mBinding = vb
        setContentView(vb.root)

        initListener()
        startAutoClickService()
    }

    private fun startAutoClickService() {
        val intent = Intent(this, NongYaoAutoClickService::class.java)
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
            WindowMgr.floatingSetting = WindowMgr.floatingSetting ?: FloatingSettingView()
            WindowMgr.floatingSetting?.show()
            WindowMgr.floatingStep = WindowMgr.floatingStep ?: FloatingStepView()
            WindowMgr.floatingStep?.show()
        }

        mBinding.closeFloatViewBtn.onClick {

        }

        mBinding.startAutoClickBtn.onClick {

        }

        btn_floating_window.setOnClickListener {
            checkFloatingWindow()
        }

        btn_show_window.setOnClickListener {
            hideKeyboard()
            if (TextUtils.isEmpty(et_interval.text.toString())) {
                Snackbar.make(et_interval, "请输入间隔", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            showFloatingWindow(et_interval.text.toString().toLong())
        }

        btn_close_window.setOnClickListener {
            closeFloatWindow()
        }

        btn_test.setOnClickListener {
            Log.d(TAG, "btn_test on click")
        }
    }
}