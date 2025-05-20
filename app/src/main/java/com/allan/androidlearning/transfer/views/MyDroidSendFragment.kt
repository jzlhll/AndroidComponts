package com.allan.androidlearning.transfer.views

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentMyDroidSendBinding
import com.allan.androidlearning.databinding.MydroidSendClientBinding
import com.allan.androidlearning.transfer.MyDroidConst
import com.allan.androidlearning.transfer.benas.MyDroidMode
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.au.module_android.simplelivedata.asNoStickLiveData
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.visible

class MyDroidSendFragment : BindingFragment<FragmentMyDroidSendBinding>() {
    private lateinit var entryFileList: List<UriRealInfoEx>

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        requireActivity().transparentStatusBar(statusBarTextDark = false) {  insets, statusBarsHeight, _ ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finishAfterTransition()
        }

        parseEntryFileList()
        clientLiveDataInit()

        val fmt = getString(R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, "")

        MyDroidConst.ipPortData.observe(this) { info->
            if (info == null || info.ip.isEmpty()) {
                binding.title.text = "请连接WI-FI或者开启热点"
            } else {
                if (info.httpPort == null) {
                    binding.title.text = info.ip
                } else if (MyDroidConst.serverIsOpen) {
                    binding.title.text = "局域网内访问：" + info.ip + ":" + info.httpPort
                } else {
                    binding.title.text = info.ip + ":" + info.httpPort
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            requireActivity().setTurnScreenOn(true)
        }
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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
                    .setBackground(requireContext().getColor(clientInfo.colorIcon))
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

        MyDroidConst.shareReceiverUriMap.asNoStickLiveData().observeUnStick(this) {

        }
    }

    override fun onStart() {
        MyDroidConst.myDroidMode = MyDroidMode.Send
        super.onStart()
    }

    fun parseEntryFileList() {
        val entryList = MyDroidConst.shareReceiverUriMap.realValue?.values
        entryFileList = entryList?.map {
            UriRealInfoEx.copyFrom(it)
        } ?: listOf()

        val sb = StringBuilder()
        entryFileList.forEach {
            sb.append(it.goodName()).append("(").append(it.fileSizeStr).append(")").append("\n")
        }
        binding.transferInfo.text =  "传输列表:\n" + sb
    }

    private val sendClientBindings = mutableListOf<MydroidSendClientBinding>()
    private fun clientItem(index:Int) : MydroidSendClientBinding{
        var binding = sendClientBindings.getOrNull(index)
        if (binding != null) {
            return binding
        }

        binding = MydroidSendClientBinding.inflate(layoutInflater)
        sendClientBindings.add(binding)
        return binding
    }
}