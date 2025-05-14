package com.allan.androidlearning.transfer.views

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.androidlearning.databinding.ActivityMyDroidReceiveShareBinding
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.androidlearning.transfer.MyDroidKeepLiveService
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.ToolbarMenuManager
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.NotificationUtil
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.transparentStatusBar
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_androidui.toast.ToastBuilder

class ShareReceiverFragment : BindingFragment<ActivityMyDroidReceiveShareBinding>() {
    private val adapter = ShareReceiverAdapter()

    private val menuMgr by unsafeLazy { ToolbarMenuManager(this, binding.toolbar,
        com.allan.androidlearning.R.menu.menu_next,
        Color.WHITE) { menuItem->
        when (menuItem.itemId) {
            com.allan.androidlearning.R.id.next -> {
                if (binding.empty.isVisible) {
                    ToastBuilder().setOnActivity(requireActivity()).setMessage("暂无文件。").setIcon("info").toast()
                } else {
                    FragmentShellActivity.start(requireActivity(), MyDroidSendFragment::class.java)
                }
            }
        }
    }}

    val permissionUtil = NotificationUtil.createPostNotificationPermissionResult(this)

    override fun onDestroy() {
        logdNoFile {"on destrory"}
        super.onDestroy()

        MyDroidKeepLiveService.Companion.stopMyDroidAlive()
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        logdNoFile {"onBinding Created"}
        NotificationUtil.requestPermission(permissionUtil) {
            MyDroidKeepLiveService.Companion.keepMyDroidAlive()
        }

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finishAfterTransition()
        }

        menuMgr.showMenu()

        requireActivity().transparentStatusBar(statusBarTextDark = false) { insets, statusBarsHeight, navigationBarHeight ->
            binding.toolbar.layoutParams.asOrNull<ConstraintLayout.LayoutParams>()?.let { toolbarLP->
                toolbarLP.topMargin = statusBarsHeight
                binding.toolbar.layoutParams = toolbarLP
            }
            insets
        }

        initRcv()
    }

    private fun initRcv() {
        binding.rcv.adapter = adapter
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.setHasFixedSize(true)

        MyDroidGlobalService.shareReceiverUriMap.observe(this) { map->
            val list = ArrayList<UriRealInfoEx>()
            list.addAll(map.values)
            adapter.submitList(list, false)
            if (list.isEmpty()) {
                binding.empty.visible()
            } else {
                binding.empty.gone()
            }
        }
    }
}