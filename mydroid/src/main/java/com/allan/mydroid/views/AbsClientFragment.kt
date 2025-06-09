package com.allan.mydroid.views

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.CallSuper
import androidx.viewbinding.ViewBinding
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.views.AbsLiveFragment.Companion.showExitDialogLater
import com.au.module_android.ui.bindings.BindingFragment

abstract class AbsClientFragment<VB: ViewBinding> : BindingFragment<VB>() {
    val alwaysScreenOn: Boolean = true
    val autoExistLongTimeInActive: Boolean = true

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
    }
}