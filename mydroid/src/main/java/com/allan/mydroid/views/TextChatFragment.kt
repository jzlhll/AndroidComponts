package com.allan.mydroid.views

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.allan.mydroid.R
import com.allan.mydroid.databinding.FragmentTextChatBinding
import com.au.module_android.utils.ImeHelper
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.transparentStatusBar

class TextChatFragment : AbsLiveFragment<FragmentTextChatBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)

        var helper = ImeHelper.assist(requireActivity(), true)
        helper?.setOnImeListener { imeOffset: Int,
                                   imeMaxHeight: Int/*包含导航栏和状态栏总高度*/,
                                   statusBarHeight: Int,
                                   navigationBarHeight: Int ->

        }

        requireActivity().transparentStatusBar(statusBarTextDark = false) { insets, statusBarsHeight, _ ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }

        val fmt = getString(R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, "")
    }

    override fun isAutoHideIme() = true
}