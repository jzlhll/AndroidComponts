package com.allan.mydroid.views.textchat

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.databinding.FragmentTextChatBinding
import com.au.module_android.click.onClick
import com.au.module_android.utils.ImeHelper
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.setMaxLength
import com.au.module_android.utils.transparentStatusBar

abstract class TextChatCommon(val f: Fragment, val binding: FragmentTextChatBinding) {
    fun onCreate() {
        binding.edit.setMaxLength(Int.MAX_VALUE)

        val ac = f.requireActivity()
        val helper = ImeHelper.assist(ac, true)
        helper?.setOnImeListener { imeOffset: Int,
                                   imeMaxHeight: Int/*包含导航栏和状态栏总高度*/,
                                   statusBarHeight: Int,
                                   navigationBarHeight: Int ->
            //imeOffset 1087 imeMaxH 1090 stH 122 navH 0
            binding.bottomHost.translationY = -imeOffset.toFloat()
        }

        ac.transparentStatusBar(statusBarTextDark = false) { insets, statusBarsHeight, _ ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }

        initSendButton()
    }

    abstract fun send(bean : WSChatMessageBean)

    abstract fun createBean(content: WSChatMessageBean.Content) : WSChatMessageBean

    private fun initSendButton() {
        binding.sendBtn.onClick {
            val text = binding.edit.text.toString()
            if (text.isNotEmpty()) {
                binding.edit.clearFocus()
                binding.edit.setText("")
                send(createBean(WSChatMessageBean.Content(text, null))) //todo file
            }
        }
    }
}