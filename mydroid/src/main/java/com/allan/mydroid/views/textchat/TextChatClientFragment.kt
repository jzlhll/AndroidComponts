package com.allan.mydroid.views.textchat

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.allan.mydroid.R
import com.allan.mydroid.databinding.FragmentTextChatBinding
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.views.AbsClientFragment
import com.au.module_android.click.onClick
import com.au.module_android.utils.ImeHelper
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.setMaxLength
import com.au.module_android.utils.transparentStatusBar

class TextChatClientFragment : AbsClientFragment<FragmentTextChatBinding>() {
    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)

        binding.toolbar.setTitle(R.string.text_chat_client_next)

        binding.toolbarCenter.onClick {

        }

        binding.edit.setMaxLength(Int.MAX_VALUE)

        val helper = ImeHelper.assist(requireActivity(), true)
        helper?.setOnImeListener { imeOffset: Int,
                                   imeMaxHeight: Int/*包含导航栏和状态栏总高度*/,
                                   statusBarHeight: Int,
                                   navigationBarHeight: Int ->
            //imeOffset 1087 imeMaxH 1090 stH 122 navH 0
            binding.bottomHost.translationY = -imeOffset.toFloat()
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

        ipObserver()
    }

    private fun ipObserver() {
        MyDroidConst.ipPortData.observe(this) { ipInfo->
            if (ipInfo == null || ipInfo.ip.isEmpty()) {
                binding.descTitle.setText(R.string.connect_wifi_or_hotspot)
            } else {
                val fmt = getString(R.string.not_close_window)
                binding.descTitle.text = String.format(fmt, ipInfo.ip)
            }
        }
    }

    override fun isAutoHideIme() = true
}