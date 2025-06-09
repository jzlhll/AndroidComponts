package com.allan.mydroid.views

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.allan.mydroid.R
import com.allan.mydroid.globals.MyDroidConst
import com.au.module_android.Globals
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_androidui.dialogs.ConfirmBottomSingleDialog

abstract class AbsLiveFragment<VB: ViewBinding> : BindingFragment<VB>() {
    companion object {
        fun showExitDialogLater() {
            Globals.mainHandler.postDelayed({
                Globals.topActivity.asOrNull<AppCompatActivity>()?.let { ac->
                    ConfirmBottomSingleDialog.Companion.show(ac.supportFragmentManager,
                        ac.getString(R.string.tips),
                        ac.getString(R.string.inactivity_message),
                        ac.getString(R.string.action_confirm),
                    ) {
                        it.dismissAllowingStateLoss()
                    }
                }
            }, 1500)
        }
    }

    val whenIpNullShowExitDialog: Boolean = true
    val alwaysScreenOn: Boolean = true
    val autoExistLongTimeInActive: Boolean = true

    var waitDialog:ConfirmBottomSingleDialog? = null

    @CallSuper
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        if (alwaysScreenOn) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                requireActivity().setTurnScreenOn(true)
            }
            requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        if (autoExistLongTimeInActive) {
            MyDroidConst.aliveStoppedData.observeUnStick(this) {
                requireActivity().finishAfterTransition()
                showExitDialogLater()
            }
        }

        if (whenIpNullShowExitDialog) {
            MyDroidConst.ipPortData.observe(this) { ipInfo->
                if (ipInfo?.ip.isNullOrEmpty()) {
                    if (waitDialog == null) {
                        ConfirmBottomSingleDialog.show(childFragmentManager, getString(R.string.tips),
                            getString(R.string.exit_with_wifi_reminder),
                            "OK",
                            true) { d->
                            waitDialog?.dismissAllowingStateLoss()
                            waitDialog = null
                            requireActivity().finishAfterTransition()
                        }.also { d->
                            d.isCancelable = false
                            waitDialog = d
                        }
                    }
                } else {
                    waitDialog?.dismissAllowingStateLoss()
                    waitDialog = null
                }
            }
        }
    }
}