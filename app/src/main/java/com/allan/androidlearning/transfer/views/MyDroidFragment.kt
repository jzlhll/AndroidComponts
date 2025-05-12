package com.allan.androidlearning.transfer.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.allan.androidlearning.databinding.FragmentMyDroidAllBinding
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.gone
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmBottomSingleDialog
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@EntryFrgName(priority = 12)
class MyDroidFragment : BindingFragment<FragmentMyDroidAllBinding>() {
    var waitDialog:ConfirmBottomSingleDialog? = null

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.receiveFileLogicBtn.onClick {
            if (ifGotoMgrAll()) {
                FragmentShellActivity.start(requireActivity(), MyDroidReceiverFragment::class.java)
            }
        }
        binding.sendFileLogicBtn.onClick {
            if (ifGotoMgrAll()) {
                //FragmentShellActivity.start(requireActivity(), MyDroidSendFragment::class.java)
            }
        }
        binding.middleLogicBtn.onClick {

        }

        MyDroidGlobalService.ipPortData.observe(this) {
            if (it == null || it.ip.isEmpty()) {
                binding.title.text = "请连接WI-FI或者开启热点"
                if (waitDialog == null) {
                    ConfirmBottomSingleDialog.show(childFragmentManager, "提示",
                        "即将退出，请连接WI-FI或者开启热点，然后重新进入。",
                        "OK",
                        true) { d->
                        clickOnWaitDialogOk()
                    }.also { d->
                        d.isCancelable = false
                        waitDialog = d
                    }
                }
            } else {
                binding.title.text = it.ip + ":" + it.httpPort
                binding.logicBtnsHost.visible()
                binding.loading.gone()
                waitDialog?.dismissAllowingStateLoss()
                waitDialog = null
            }
        }
    }

    private var isWaitOpenWifiOrAp = false

    private fun clickOnWaitDialogOk() {
        isWaitOpenWifiOrAp = true
        lifecycleScope.launch {
            var count = 0
            while (count++ < 10) {
                MyDroidGlobalService.getIpAddressAndStartServer()
                delay(250)
                waitDialog?.dismissAllowingStateLoss()
                waitDialog = null

                if (!binding.loading.isVisible) {
                    break
                }
            }

            if (binding.loading.isVisible) {
                requireActivity().finishAfterTransition()
            }
        }
    }

    private fun ifGotoMgrAll() : Boolean{
        if (false && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
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

    override fun toolbarInfo() = ToolbarInfo("MyDroid传输工具集")
}