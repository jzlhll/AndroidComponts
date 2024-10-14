package com.allan.androidlearning.androidui

import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import com.allan.androidlearning.databinding.FragmentAndroidUiEditBinding
import com.au.module_android.postToMainHandler
import com.au.module_android.toast.ToastUtil
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.toolbar.ToolbarInfo
import com.au.module_android.utils.transparentStatusBar
import com.au.module_androidui.toast.ToastBuilder
import com.au.module_androidui.widget.CustomTextInputLayout

/**
 * @author allan
 * @date :2024/9/12 10:14
 * @description:
 */
class AndroidUiEditFragment : BindingFragment<FragmentAndroidUiEditBinding>() {
    private val randomId = Math.random()
    private val enableEdge = if(randomId > 0.666) 1 else if(randomId > 0.333) 0 else -1

    private fun fullInfo() : String{
        return when (enableEdge) {
            1 -> "enableEdgeToEdge"
            0 -> "nothing"
            -1 -> "transparentBars"
            else -> throw RuntimeException()
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.inputLayoutEmail.checkInputFun = {
            CustomTextInputLayout.matcherEmail(it)
        }

        binding.inputLayoutPassword.checkInputFun = {
            CustomTextInputLayout.matcherPassword(it)
        }
        val msg = if (Math.random() > 0.5) {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            "Adjust Resize"
        } else {
            requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN or WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)
            "Adjust Pan"
        }
        setTitle("Edit")

        binding.infoText.text = msg + " NestedScrollView " + fullInfo()

        if (enableEdge == -1) {
            requireActivity().transparentStatusBar { insets, statusBarsHeight, navigationBarHeight -> insets }
        }
    }

    override fun fullWindowSetEdgeToEdge(): Boolean {
        return enableEdge == 1
    }

    override fun toolbarInfo(): ToolbarInfo {
        return ToolbarInfo()
    }

    override fun isAutoHideIme(): Boolean {
        return true
    }
}