package com.allan.androidlearning.transfer.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.core.net.toUri
import com.allan.androidlearning.databinding.FragmentMyDroidAllBinding
import com.allan.androidlearning.transfer.MyDroidGlobalService
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.startActivityFix
import com.au.module_androidui.dialogs.ConfirmCenterDialog

@EntryFrgName(priority = 12)
class MyDroidFragment : BindingFragment<FragmentMyDroidAllBinding>() {
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
            binding.title.text = it.ip + ":" + it.httpPort
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