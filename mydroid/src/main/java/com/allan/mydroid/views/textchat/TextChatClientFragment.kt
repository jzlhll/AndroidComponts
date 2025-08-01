package com.allan.mydroid.views.textchat

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.allan.mydroid.R
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.databinding.FragmentTextChatBinding
import com.allan.mydroid.globals.LifeSimpleNetworkObserver
import com.allan.mydroid.utils.BlurViewEx
import com.allan.mydroid.views.textchat.uibean.NormalItem
import com.au.module_android.json.toJsonString
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.logd
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog
import kotlinx.coroutines.launch

class TextChatClientFragment : BindingFragment<FragmentTextChatBinding>() {
    private var mIp:String? = null

    init {
        LifeSimpleNetworkObserver(this).apply {
            onChanged = { ip->
                mIp = ip
                uploadMyIp()
            }
        }
    }

    private fun uploadMyIp() {
        lifecycleScope.launch {
            if (!mIp.isNullOrEmpty()) {
                binding.descTitle.text = mIp + " (" + viewModel.serverInfo() + ")"
            } else {
                binding.descTitle.setText(R.string.connect_wifi_or_hotspot)
            }
        }
    }

    private val viewModel by lazy { ViewModelProvider(this)[TextChatClientViewModel::class.java] }

    private val common by unsafeLazy { object : TextChatCommon(this, binding) {
        override fun buttonSend(bean: WSChatMessageBean) {
            val json = bean.toJsonString()
            viewModel.wsClient?.sendText(json)
            logd { "client send: $json" }
            //刚刚点击的事件，肯定就是自己。
            onAddChatItem(NormalItem(true).also { it.message = bean })
        }

        override fun createBean(content: WSChatMessageBean.Content): WSChatMessageBean {
            val sender = WSChatMessageBean.Sender().apply {
                name = viewModel.wsClient?.goodName() ?: "unknown"
                color = (viewModel.wsClient?.color ?: "#212121")
                isServer = false
                platform = "androidApp" //todo 增加服务平台
            }
            return WSChatMessageBean(sender, content, "sending")
        }

    } }

    private val onServerMsg:(message: WSChatMessageBean)->Unit = { bean->
        logd { "onTransferClientMsg from: ${bean.sender}" }
        var isMe = bean.sender.name == viewModel.wsClient?.goodName()
        common.onAddChatItem(NormalItem(isMe).also { it.message = bean })
    }

    override fun onStart() {
        super.onStart()
        logd { "allan onStart" }
        showLoadingAndInputDialog()
    }

    private fun showLoadingAndInputDialog(errorToast:String? = null) {
        binding.loadingHost.visible()

        if (!viewModel.isWSClientConnected()) {
            FragmentBottomSheetDialog.show<TextChatClientIpPortInputDialog>(childFragmentManager,
                bundleOf("errorToast" to errorToast),
                hasEditText = true)
        }
    }

    override fun onStop() {
        super.onStop()
        logd { "allan onStop" }
        viewModel.shutdownWSClient()
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.toolbar.setTitle(R.string.text_chat_client_next)
        BlurViewEx(binding.loadingHost, 0).setBlur(binding.root, 20f)
        common.onCreate()

        viewModel.closedData.observe(this) {
            logd { "get closedData $it" }
            showLoadingAndInputDialog(it)
        }

        val fmt = getString(R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, "")
    }

    ////////////////////////////
    /**
     * call by dialog。
     * 连接服务器。
     */
    fun connectServer(ip: String, port: Int) {
        viewModel.connectServer(ip, port,
            onTransferClientMsgCallback = onServerMsg,
            cannotOpenBlock = {
            showLoadingAndInputDialog(it)
        }) {
            lifecycleScope.launch {
                binding.loadingHost.visibility = android.view.View.GONE
                uploadMyIp()
            }
        }
    }

    override fun isAutoHideIme() = true
}