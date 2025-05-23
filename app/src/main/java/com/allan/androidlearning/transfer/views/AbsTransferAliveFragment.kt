package com.allan.androidlearning.transfer.views

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.allan.androidlearning.R
import com.allan.androidlearning.transfer.MyDroidConst
import com.au.module_android.Globals
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_androidui.dialogs.ConfirmBottomSingleDialog

abstract class AbsTransferAliveFragment<VB: ViewBinding> : BindingFragment<VB>() {
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

    @CallSuper
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            requireActivity().setTurnScreenOn(true)
        }
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        MyDroidConst.aliveStoppedData.observe(this) {
            requireActivity().finishAfterTransition()
            showExitDialogLater()
        }
    }
}