package com.allan.mydroid.views.send

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.toColorInt
import com.allan.mydroid.api.MyDroidMode
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.databinding.FragmentMyDroidSendBinding
import com.allan.mydroid.databinding.MydroidSendClientBinding
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.views.AbsLiveFragment
import com.au.module_android.Globals
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.visible
import com.au.module_androidcolor.R

class MyDroidSendFragment : AbsLiveFragment<FragmentMyDroidSendBinding>() {
    private lateinit var entryFileList: List<UriRealInfoEx>

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        super.onBindingCreated(savedInstanceState)
        binding.adHost.setColor(Globals.getColor(R.color.color_normal_block0))
        binding.adHost.startAnimation()

        requireActivity().transparentStatusBar(statusBarTextDark = false) { insets, statusBarsHeight, _ ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finishAfterTransition()
        }

        clientLiveDataInit()

        val fmt = getString(com.allan.mydroid.R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, "")

        MyDroidConst.ipPortData.observe(this) { info->
            if (info == null || info.ip.isEmpty()) {
                binding.title.setText(com.allan.mydroid.R.string.connect_wifi_or_hotspot)
            } else {
                if (info.httpPort == null) {
                    binding.title.text = info.ip
                } else if (MyDroidConst.serverIsOpen) {
                    binding.title.text = String.format(getString(com.allan.mydroid.R.string.lan_access_fmt), info.ip, "" + info.httpPort)
                } else {
                    binding.title.text = info.ip + ":" + info.httpPort
                }
            }
        }

    }

    private fun clientLiveDataInit() {
        MyDroidConst.clientListLiveData.observe(this) { clientList ->
            for (clientBinding in sendClientBindings) {
                clientBinding.root.gone()
            }

            clientList.forEachIndexed { index, clientInfo ->
                logdNoFile { "client List[$index] = $clientInfo" }
                val item = clientItem(index)
                item.title.text = clientInfo.clientName
                item.icon.background = ViewBackgroundBuilder()
                    .setBackground(clientInfo.color.toColorInt())
                    .setCornerRadius(32f.dp)
                    .build()
                if (!item.root.isAttachedToWindow) {
                    binding.clientsHost.addView(item.root)
                }
                item.root.visible()
            }

//            sendClientBindings.forEachIndexed { index, binding ->
//                binding.check.isChecked = index == 0
//            }
        }

        MyDroidConst.sendUriMap.observe(this) { //监听没问题
            parseEntryFileList()
        }
    }

    override fun onStart() {
        MyDroidConst.currentDroidMode = MyDroidMode.Send
        super.onStart()
    }

    fun parseEntryFileList() {
        val entryList = MyDroidConst.sendUriMap.realValue?.values?.filter { it.isChecked }
        entryFileList = entryList?.map {
            UriRealInfoEx.Companion.copyFrom(it)
        } ?: listOf()

        val sb = StringBuilder()
        entryFileList.forEach {
            sb.append(it.goodName()).append("(").append(it.fileSizeStr).append(")").append("\n")
        }
        binding.transferInfo.text = sb
    }

    private val sendClientBindings = mutableListOf<MydroidSendClientBinding>()
    private fun clientItem(index:Int) : MydroidSendClientBinding {
        var binding = sendClientBindings.getOrNull(index)
        if (binding != null) {
            return binding
        }

        binding = MydroidSendClientBinding.inflate(layoutInflater)
        sendClientBindings.add(binding)
        return binding
    }
}