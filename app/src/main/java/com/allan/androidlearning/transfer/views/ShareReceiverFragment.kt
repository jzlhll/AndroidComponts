package com.allan.androidlearning.transfer.views

import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.allan.androidlearning.databinding.ActivityMyDroidReceiveShareBinding
import com.allan.androidlearning.transfer.KEY_AUTO_ENTER_SEND_VIEW
import com.allan.androidlearning.transfer.MyDroidConst
import com.allan.androidlearning.transfer.MyDroidKeepLiveService
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.au.module_android.permissions.PermissionStorageHelper.MediaType.AUDIO
import com.au.module_android.permissions.PermissionStorageHelper.MediaType.IMAGE
import com.au.module_android.permissions.PermissionStorageHelper.MediaType.VIDEO
import com.au.module_android.permissions.createStoragePermissionForResult
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.ToolbarMenuManager
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.NotificationUtil
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.gone
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.startActivityFix
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
    private val perResult = createStoragePermissionForResult(arrayOf(IMAGE, AUDIO, VIDEO))

    private val adapter = ShareReceiverAdapter()

    private var mAutoNextJob: Job? = null
    private var mDelayCancelDialog:ConfirmBottomSingleDialog? = null
    private var mDelayTime = 2

    private fun dialogContent() : String{
        return "即将在${mDelayTime}秒后自动进入下一步，你可以点击取消，勾掉一些文件。"
    }

    private val menuMgr by unsafeLazy { ToolbarMenuManager(this, binding.toolbar,
        com.allan.androidlearning.R.menu.menu_next,
        Color.WHITE) { menuItem->
        when (menuItem.itemId) {
            com.allan.androidlearning.R.id.next -> {
                if (binding.empty.isVisible) {
                    ToastBuilder().setOnActivity(requireActivity()).setMessage("暂无文件，从相册，文件管理器或其他应用分享进来吧。").setIcon("info").toast()
                } else {
                    jumpIntoMyDroidSend()
                }
            }
        }
    }}

    private fun ifGotoMgrAll() : Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val ex = Environment.isExternalStorageManager()
            if (!ex) {
                ConfirmCenterDialog.show(childFragmentManager,
                    "应用管理权限",
                    "该功能需要全局设置权限，即将跳转，打开该功能。",
                    "OK") {
                    gotoMgrAll()
                    it.dismissAllowingStateLoss()
                }
            }
            return ex
        }

        return true
    }

    private fun gotoMgrAll() {
        val intent = Intent().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                action = Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
            }
            data = "package:${requireContext().packageName}".toUri()
        }
        startActivityFix(intent)
    }

    private fun jumpIntoMyDroidSend() {
        mDelayCancelDialog?.dismissAllowingStateLoss()
        mDelayCancelDialog = null

        perResult.safeRun({
            if (ifGotoMgrAll()) {
                FragmentShellActivity.start(requireActivity(), MyDroidSendFragment::class.java)
            }
        }, notGivePermissionBlock = {
            Toast(requireActivity()).also {
                it.setText("未授权。")
            }.show()
        })
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
            if (!binding.empty.isVisible) {
                jumpIntoMyDroidSend()
            }
        }
    }

    private fun initRcv() {
        binding.rcv.adapter = adapter
        binding.rcv.layoutManager = LinearLayoutManager(binding.rcv.context)
        binding.rcv.setHasFixedSize(true)

        MyDroidConst.shareReceiverUriMap.observe(this) { map->
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