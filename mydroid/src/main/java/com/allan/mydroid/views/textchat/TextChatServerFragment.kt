package com.allan.mydroid.views.textchat

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import com.allan.mydroid.R
import com.allan.mydroid.beans.MyDroidMode
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.databinding.FragmentTextChatBinding
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.views.AbsLiveFragment
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utils.ImeHelper
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.setMaxLength
import com.au.module_android.utils.transparentStatusBar

class TextChatServerFragment : AbsLiveFragment<FragmentTextChatBinding>() {
    private fun clickOnSendBtn() {
        val text = binding.edit.text.toString()
        if (text.isNotEmpty()) {
            binding.edit.clearFocus()
            binding.edit.setText("")
            //todo 现在就发送文字，附件先不管
            val sender = WSChatMessageBean.Sender().apply {
                name = MyDroidConst.serverName
                color = Globals.getColor(R.color.logic_text_chat_server)
                isServer = true
                platform = "androidApp" //todo 增加服务平台
            }
            val content = WSChatMessageBean.Content(text, null)
            val bean = WSChatMessageBean(sender, content, "delivered")
            MyDroidGlobalService.websocketServer?.serverSendTextChatMessage(bean)
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        binding.loadingHost.gone()

        val titleFmt = getString(R.string.text_chat_server_next)
        binding.toolbar.setTitle(String.format(titleFmt, 0))
        MyDroidConst.clientListLiveData.observe(this) { clientList->
            val titleFmt = getString(R.string.text_chat_server_next)
            binding.toolbar.setTitle(String.format(titleFmt, clientList.size))
        }

        binding.toolbarCenter.onClick {

        }

        binding.enterBtn.onClick {
            clickOnSendBtn()
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
                if (MyDroidConst.serverIsOpen) {
                    val fmt = getString(R.string.not_close_window)
                    binding.descTitle.text = String.format(fmt, ipInfo.ip + ":" + ipInfo.httpPort)
                } else {
                    binding.descTitle.setText(R.string.something_error)
                }
            }
        }
    }

    override fun isAutoHideIme() = true

    override fun onStart() {
        super.onStart()
        MyDroidConst.currentDroidMode = MyDroidMode.TextChat
    }
}