package com.allan.mydroid.views.textchat

import android.os.Bundle
import com.allan.mydroid.R
import com.allan.mydroid.api.MyDroidMode
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.databinding.FragmentTextChatBinding
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.views.AbsLiveFragment
import com.allan.mydroid.views.send.SendListSelectorDialog
import com.allan.mydroid.views.textchat.uibean.NormalItem
import com.au.module_android.Globals
import com.au.module_android.utils.gone
import com.au.module_android.utils.unsafeLazy

class TextChatServerFragment : AbsLiveFragment<FragmentTextChatBinding>(), SendListSelectorDialog.ISelectItemClick {
    private val common : TextChatCommon by unsafeLazy {
        object : TextChatCommon(this, binding) {
            override fun createBean(content: WSChatMessageBean.Content): WSChatMessageBean {
                val sender = WSChatMessageBean.Sender().apply {
                    name = MyDroidConst.serverName
                    color = Globals.getString(com.au.module_androidcolor.R.string.color_text_normal_str)
                    isServer = true
                    platform = "androidApp" //todo 增加服务平台
                }
                return WSChatMessageBean(sender, content, "delivered")
            }

            override fun buttonSend(bean: WSChatMessageBean) {
                //我自己的点击消息
                MyDroidGlobalService.websocketServer?.serverSendTextChatMessage(bean)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        MyDroidConst.currentDroidMode = MyDroidMode.TextChat
        //接收中转的msg
        MyDroidGlobalService.websocketServer?.onTransferBothMsgCallback = { bean->
            val isMe = bean.sender.isServer //这里就是服务器收到的消息，直接判断sender是不是服务器的消息类型，就认为是自己即可
            val bean = NormalItem(isMe).also { it.message = bean }
            common.onAddChatItem(bean)
        }
    }

    override fun onStop() {
        super.onStop()
        MyDroidGlobalService.websocketServer?.onTransferBothMsgCallback = null
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        binding.loadingHost.gone()
        val titleFmt = getString(R.string.text_chat_server_next)
        binding.toolbar.setTitle(String.format(titleFmt, 0))

        common.onCreate()

        MyDroidConst.clientListLiveData.observe(this) { clientList->
            val titleFmt = getString(R.string.text_chat_server_next)
            binding.toolbar.setTitle(String.format(titleFmt, clientList.size))
        }

        val fmt = getString(R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, "")

        initIpShow()
    }

    private fun initIpShow() {
        //必须通过监听来显示。开启server后，才会变更参数。
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

    override fun onItemClick(bean: UriRealInfoEx) {
        common.buttonSend(common.createBean(WSChatMessageBean.Content("", bean.copyToHtml())))
    }
}