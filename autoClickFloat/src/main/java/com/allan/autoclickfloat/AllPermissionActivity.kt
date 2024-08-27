package com.allan.autoclickfloat

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.allan.autoclickfloat.accessibility.AutoClickFloatAccessService
import com.allan.autoclickfloat.activities.startup.AllFeaturesFragment
import com.allan.autoclickfloat.activities.startup.OnlyFloatPermissionViewModel
import com.allan.autoclickfloat.activities.startup.PermissionsRequestFragment
import com.allan.autoclickfloat.consts.Const
import com.allan.autoclickfloat.databinding.RootActivityBinding
import com.au.module_android.ui.bindings.BindingActivity
import com.au.module_android.utils.launchOnUi
import com.au.module_android.utils.replaceFragment
import com.au.module_android.utils.unsafeLazy
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/**
 * @author allan
 * @date :2024/4/17 11:43
 * @description:
 */
class AllPermissionActivity : BindingActivity<RootActivityBinding>() {
    private val viewModel by unsafeLazy { ViewModelProvider(this)[OnlyFloatPermissionViewModel::class.java] }

    private var _permissionsRequestFragment: PermissionsRequestFragment? = null
    private val permissionsRequestFragment: PermissionsRequestFragment
        get() {
            if (_permissionsRequestFragment == null) {
                _permissionsRequestFragment = PermissionsRequestFragment()
            }
            return _permissionsRequestFragment!!
        }

    private var _allFeaturesFragment: AllFeaturesFragment? = null
    private val allFeaturesFragment: AllFeaturesFragment
        get() {
            if (_allFeaturesFragment == null) {
                _allFeaturesFragment = AllFeaturesFragment()
            }
            return _allFeaturesFragment!!
        }

    private var isPermissionFragment:Boolean? = null
    private var enabledFromServiceJob:Job? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d(Const.TAG, "onDestroy: ")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(Const.TAG, "onCreate: ")
        viewModel.allPermissionEnabled.observeUnStick(this) { it ->
            when (it) {
                OnlyFloatPermissionViewModel.STATE_NO_FLOAT_WINDOW -> {
                    showPermissionsRequest()
                }
                OnlyFloatPermissionViewModel.STATE_ALL_PERMISSION_ENABLE -> {
                    showAllFeatures()
                }
            }
        }

        AutoClickFloatAccessService.isEnabledLiveData.observe(this) {
            enabledFromServiceJob?.cancel()
            enabledFromServiceJob = lifecycleScope.launchOnUi {
                delay(1000)
                viewModel.getPermission(this@AllPermissionActivity)
                delay(2000)
                viewModel.getPermission(this@AllPermissionActivity)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getPermission(this)
    }

    fun showPermissionsRequest() {
        if (isPermissionFragment == null || isPermissionFragment == false) {
            isPermissionFragment = true
            replaceFragment(R.id.fragmentRoot, permissionsRequestFragment)
        }
    }

    fun showAllFeatures() {
        if (isPermissionFragment == null || isPermissionFragment == true) {
            isPermissionFragment = false
            replaceFragment(R.id.fragmentRoot, allFeaturesFragment)
        }
    }

    companion object {
        fun checkGotoAllPermissionActivity(fragment: Fragment) {
            val ac = fragment.requireActivity()
            if (!OnlyFloatPermissionViewModel.isFloatWindowEnabled(ac)) {
                ConfirmCenterDialog.show(fragment.childFragmentManager,
                    "请授权悬浮窗权限。",
                    "点击回到首页，重新申请权限。",
                    "好的") {
                    val activity = fragment.requireActivity()
                    activity.finishAfterTransition()
                    fragment.startActivity(Intent(activity, AllPermissionActivity::class.java))
                }
            }
        }
    }
}