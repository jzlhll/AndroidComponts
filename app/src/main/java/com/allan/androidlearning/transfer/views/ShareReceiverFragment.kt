package com.allan.androidlearning.transfer.views

import android.graphics.Color
import android.os.Bundle
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.androidlearning.databinding.ActivityMyDroidReceiveShareBinding
import com.allan.androidlearning.transfer.KEY_AUTO_ENTER_SEND_VIEW
import com.allan.androidlearning.transfer.MyDroidConst
import com.allan.androidlearning.transfer.MyDroidKeepLiveService
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.permissions.PermissionStorageHelper
import com.au.module_android.permissions.createMultiPermissionForResult
import com.au.module_android.permissions.createStoragePermissionForResult
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
import com.au.module_androidui.dialogs.ConfirmBottomSingleDialog
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.au.module_androidui.toast.ToastBuilder
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ShareReceiverFragment : BindingFragment<ActivityMyDroidReceiveShareBinding>() {
    private val adapter = ShareReceiverAdapter()

    private var mAutoNextJob: Job? = null
    private var mDelayCancelDialog:ConfirmBottomSingleDialog? = null
    private var mDelayTime = 2

    private val permissionResult = createStoragePermissionForResult(
            arrayOf(PermissionStorageHelper.MediaType.AUDIO,
                PermissionStorageHelper.MediaType.IMAGE,
                PermissionStorageHelper.MediaType.VIDEO,)
        )

    private fun dialogContent() : String{
        return "即将在${mDelayTime}秒后自动进入下一步，你可以点击取消，勾掉一些文件。"
    }

    private val menuMgr by unsafeLazy { ToolbarMenuManager(this, binding.toolbar,
        com.allan.androidlearning.R.menu.menu_next,
        Color.WHITE) { menuItem->
        when (menuItem.itemId) {
            com.allan.androidlearning.R.id.next -> {
                when (isEmpty()) {
                    2-> {
                        ToastBuilder().setOnActivity(requireActivity()).setMessage("请勾选文件。").setIcon("info").toast()
                    }
                    1-> {
                        jumpIntoMyDroidSend()
                    }
                    else -> {
                        ToastBuilder().setOnActivity(requireActivity()).setMessage("暂无文件，从相册，文件管理器或其他应用分享进来吧。").setIcon("info").toast()
                    }
                }
            }
        }
    }}

    /**
     * 1表示有数据。2表示有数据，但是全都没勾；0表示无数据。
     */
    private fun isEmpty() : Int{
        val values = MyDroidConst.sendUriMap.realValue?.values
        if (values == null || values.size == 0) {
            return 0
        }
        return if(values.find { it.isChecked } != null) 1 else 2
    }

    private fun jumpIntoMyDroidSend() {
        mDelayCancelDialog?.dismissAllowingStateLoss()
        mDelayCancelDialog = null

        val helper = PermissionStorageHelper()
        if(helper.ifGotoMgrAll {
            ConfirmCenterDialog.show(childFragmentManager,
                "应用管理权限",
                "该功能需要全局设置权限，即将跳转，打开该功能。",
                "OK") {
                helper.gotoMgrAll(requireActivity())
                it.dismissAllowingStateLoss()
            }
        }) {
            permissionResult.safeRun({
                FragmentShellActivity.start(requireActivity(), MyDroidSendFragment::class.java)
            }, notGivePermissionBlock = {
                ToastBuilder().setMessage("请授权媒体权限，否则，无法访问文件。").setIcon("warn").setOnTop().toast()
            })
        }
    }

    val permissionUtil = NotificationUtil.createPostNotificationPermissionResult(this)

    override fun onDestroy() {
        logdNoFile {"on destrory"}
        super.onDestroy()

        MyDroidKeepLiveService.Companion.stopMyDroidAlive()
    }

    private val autoImport by unsafeLazy { arguments?.getBoolean(KEY_AUTO_ENTER_SEND_VIEW) == true }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        logdNoFile {"onBinding Created"}
        NotificationUtil.requestPermission(permissionUtil) {
            MyDroidKeepLiveService.Companion.keepMyDroidAlive()
        }

        binding.adHost.setColor(Globals.getColor(com.au.module_androidcolor.R.color.color_normal_block0))
        binding.adHost.startAnimation()

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finishAfterTransition()
        }

        binding.infoText.onClick {
            ConfirmBottomSingleDialog.show(childFragmentManager,
                "免责申明",
                "从系统相册，文件管理器导入的文件，仅仅做文件名和大小解析，不会复制。从其他应用导入的会临时保存在缓存中，用完也会删除。\n下次app启动时，您需要重新分享导入。",
                "知道了") {
                it.dismissAllowingStateLoss()
            }
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

        if (autoImport) autoImportAction()
    }

    private fun autoImportAction() {
        mDelayCancelDialog = ConfirmBottomSingleDialog.show(childFragmentManager,
            "自动下一步",
            dialogContent(),
            getString(com.au.module_android.R.string.cancel),
        ) {
            mAutoNextJob?.cancel()
            it.dismissAllowingStateLoss()
        }

        mAutoNextJob = lifecycleScope.launch {
            while(mDelayTime-- > 0) {
                delay(1000)
                mDelayCancelDialog?.changeContent(dialogContent())
            }
            //自动跳入
            if (isEmpty() == 1) {
                jumpIntoMyDroidSend()
            }
        }
    }

    private fun initRcv() {
        binding.rcv.adapter = adapter
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.setHasFixedSize(true)

        MyDroidConst.sendUriMap.observe(this) { map->
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