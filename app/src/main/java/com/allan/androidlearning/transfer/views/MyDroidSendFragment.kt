package com.allan.androidlearning.transfer.views

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentMyDroidSendBinding
import com.allan.androidlearning.databinding.MydroidSendClientBinding
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.benas.MyDroidMode
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.transparentStatusBar

class MyDroidSendFragment : BindingFragment<FragmentMyDroidSendBinding>() {

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finishAfterTransition()
        }

        MyDroidGlobalService.clientListLiveData.observe(this) { clientList->
            clientList.forEachIndexed { index, string ->
                logdNoFile { "client List[$index] = $string" }
                val item = clientItem(index)
                item.title.text = string
                item.icon.background = ViewBackgroundBuilder()
                    .setBackground(requireContext().getColor(R.color.client_send_1 + index))
                    .setCornerRadius(32f.dp)
                    .build()
            }
        }

        requireActivity().transparentStatusBar(statusBarTextDark = false) {  insets, statusBarsHeight, _ ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }

        val fmt = getString(R.string.not_close_window)
        binding.descTitle.text = String.format(fmt, "")

        MyDroidGlobalService.onTransferInfoData.observeUnStick(this) { info->
            binding.transferInfo.text = info
        }

        MyDroidGlobalService.ipPortData.observe(this) { info->
            if (info == null || info.ip.isEmpty()) {
                binding.title.text = "请连接WI-FI或者开启热点"
            } else {
                if (info.httpPort == null) {
                    binding.title.text = info.ip
                } else if (MyDroidGlobalService.isSuccessOpenServer) {
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

    override fun onStart() {
        MyDroidGlobalService.myDroidModeData.setValueSafe(MyDroidMode.Send)
        super.onStart()
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