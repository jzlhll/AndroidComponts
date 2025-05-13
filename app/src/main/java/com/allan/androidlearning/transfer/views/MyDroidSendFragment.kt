package com.allan.androidlearning.transfer.views

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.constraintlayout.widget.ConstraintLayout
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentMyDroidSendBinding
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utilsmedia.getExternalFreeSpace

@EntryFrgName(priority = 12)
class MyDroidSendFragment : BindingFragment<FragmentMyDroidSendBinding>() {

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finishAfterTransition()
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

}