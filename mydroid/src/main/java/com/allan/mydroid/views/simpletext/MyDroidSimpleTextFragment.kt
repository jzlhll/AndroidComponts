package com.allan.mydroid.views.simpletext

import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.mydroid.databinding.FragmentMyDroidSimpleTextBinding
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.views.AbsLiveFragment
import com.au.module_android.Globals
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.transparentStatusBar
import com.au.module_androidcolor.R

class MyDroidSimpleTextFragment : AbsLiveFragment<FragmentMyDroidSimpleTextBinding>() {
    private lateinit var adapter: SimpleTextAdapter

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

        binding.rcv.adapter = SimpleTextAdapter().also { adapter = it }
        binding.rcv.layoutManager = LinearLayoutManager(requireContext()).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        binding.rcv.itemAnimator = null
    }
}