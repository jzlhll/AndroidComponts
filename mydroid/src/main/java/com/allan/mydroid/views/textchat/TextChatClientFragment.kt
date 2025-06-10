package com.allan.mydroid.views.textchat

import android.graphics.Color
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.allan.mydroid.R
import com.allan.mydroid.beans.WSChatMessageBean
import com.allan.mydroid.databinding.FragmentTextChatBinding
import com.allan.mydroid.globals.LifeSimpleNetworkObserver
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.globals.MyDroidGlobalService
import com.allan.mydroid.utils.BlurViewEx
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.ImeHelper
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.logd
import com.au.module_android.utils.setMaxLength
import com.au.module_android.utils.transparentStatusBar
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

    fun connectServer(ip: String, port: Int) {
        viewModel.connectServer(ip, port) {
            lifecycleScope.launch {
                binding.loadingHost.visibility = android.view.View.GONE
                uploadMyIp()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        logd { "allan onStart" }
        if (!viewModel.isWSClientConnected()) {
            FragmentBottomSheetDialog.show<TextChatClientIpPortInputDialog>(childFragmentManager, hasEditText = true)
        }
    }

    override fun onStop() {
        super.onStop()
        logd { "allan onStop" }
    }

    private fun clickOnSendBtn() {
        val text = binding.edit.text.toString()
        if (text.isNotEmpty()) {
            binding.edit.clearFocus()
            binding.edit.setText("")
            //todo 现在就发送文字，附件先不管
            val sender = WSChatMessageBean.Sender().apply {
                name = MyDroidConst.serverName
                color = viewModel.wsClient?.color ?: Color.TRANSPARENT
                isServer = false
                platform = "androidApp" //todo 增加服务平台
            }
            val content = WSChatMessageBean.Content(text, null)
            val bean = WSChatMessageBean(sender, content, "sending")
            MyDroidGlobalService.websocketServer?.serverSendTextChatMessage(bean)
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        BlurViewEx(binding.loadingHost, 0).setBlur(binding.root, 20f)

        binding.toolbar.setTitle(R.string.text_chat_client_next)
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
    }

    override fun isAutoHideIme() = true
}