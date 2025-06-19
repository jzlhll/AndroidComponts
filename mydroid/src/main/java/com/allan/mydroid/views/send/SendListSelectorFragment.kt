package com.allan.mydroid.views.send

import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allan.mydroid.R
import com.allan.mydroid.globals.KEY_AUTO_ENTER_SEND_VIEW
import com.allan.mydroid.globals.MyDroidConst
import com.allan.mydroid.views.MyDroidKeepLiveService
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.databinding.ActivityMyDroidSendlistBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.permissions.PermissionStorageHelper
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
import kotlin.collections.find

class SendListSelectorFragment : BindingFragment<ActivityMyDroidSendlistBinding>() {
    private val common = object : SendListSelectorCommon(this) {
        override fun rcv() = binding.rcv

        override fun empty() = binding.empty

        override fun itemClick(bean: UriRealInfoEx?) {
        }
    }

    private var mAutoNextJob: Job? = null
    private var mDelayCancelDialog: ConfirmBottomSingleDialog? = null
    private var mDelayTime = 3

    //todo 有manageAll，则无需本地
//    private val permissionResult = createStoragePermissionForResult(
//            arrayOf(PermissionStorageHelper.MediaType.AUDIO,
//                PermissionStorageHelper.MediaType.IMAGE,
//                PermissionStorageHelper.MediaType.VIDEO,)
//        )

    private fun dialogContent() : String{
        val fmt = getString(R.string.auto_proceed_fmt)
        return String.format(fmt, mDelayTime)
    }

    private val menuMgr by unsafeLazy {
        ToolbarMenuManager(
            this, binding.toolbar,
            R.menu.menu_next,
            Color.WHITE
        ) { menuItem ->
            when (menuItem.itemId) {
                R.id.next -> {
                    when (isEmpty()) {
                        2 -> {
                            ToastBuilder().setOnActivity(requireActivity()).setMessage(
                                getString(R.string.select_files_hint)
                            ).setIcon("info").toast()
                        }

                        1 -> {
                            jumpIntoMyDroidSend()
                        }

                        else -> {
                            ToastBuilder().setOnActivity(requireActivity()).setMessage(
                                getString(R.string.empty_file_prompt)).setIcon("info").toast()
                        }
                    }
                }
            }
        }
    }

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
            ConfirmCenterDialog.Companion.show(childFragmentManager,
                getString(R.string.app_management_permission),
                getString(R.string.global_permission_prompt),
                "OK") {
                helper.gotoMgrAll(requireActivity())
                it.dismissAllowingStateLoss()
            }
        }) {
            FragmentShellActivity.Companion.start(requireActivity(), MyDroidSendFragment::class.java)

//            permissionResult.safeRun({
//                FragmentShellActivity.start(requireActivity(), MyDroidSendFragment::class.java)
//            }, notGivePermissionBlock = {
//                ToastBuilder().setMessage("请授权媒体权限，否则，无法访问文件。").setIcon("warn").setOnTop().toast()
//            })
        }
    }

    val permissionUtil = NotificationUtil.Companion.createPostNotificationPermissionResult(this)

    override fun onDestroy() {
        logdNoFile {"on destrory"}
        super.onDestroy()

        MyDroidKeepLiveService.Companion.stopMyDroidAlive()
    }

    private val autoImport by unsafeLazy { arguments?.getBoolean(KEY_AUTO_ENTER_SEND_VIEW) == true }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        logdNoFile {"onBinding Created"}
        NotificationUtil.Companion.requestPermission(permissionUtil) {
            MyDroidKeepLiveService.Companion.keepMyDroidAlive()
        }

        binding.adHost.setColor(Globals.getColor(com.au.module_androidcolor.R.color.color_normal_block0))
        binding.adHost.startAnimation()

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finishAfterTransition()
        }

        binding.infoText.onClick {
            ConfirmBottomSingleDialog.Companion.show(childFragmentManager,
                getString(R.string.disclaimer_title),
                    getString(R.string.disclaimer_content),
                getString(R.string.action_confirm)) {
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

        common.onBindingCreated()
        if (autoImport) autoImportAction()
    }

    private fun autoImportAction() {
        mDelayCancelDialog = ConfirmBottomSingleDialog.Companion.show(childFragmentManager,
            getString(R.string.action_auto_next),
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

}