package com.allan.androidlearning.transfer.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider
import com.allan.androidlearning.databinding.FragmentMyDroidAllBinding
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.permissions.PermissionMediaType.AUDIO
import com.au.module_android.permissions.PermissionMediaType.IMAGE
import com.au.module_android.permissions.PermissionMediaType.VIDEO
import com.au.module_android.permissions.createStoragePermissionForResult
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.gone
import com.au.module_android.utils.startActivityFix
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmBottomSingleDialog
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import kotlinx.coroutines.launch

@EntryFrgName(priority = 12)
class MyDroidFragment : BindingFragment<FragmentMyDroidAllBinding>() {
    private val perResult = createStoragePermissionForResult(arrayOf(IMAGE, AUDIO, VIDEO))

    var waitDialog:ConfirmBottomSingleDialog? = null

    val viewModel by unsafeLazy {
        ViewModelProvider(this)[MyDroidSendViewModel::class.java]
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.receiveFileLogicBtn.onClick {
            FragmentShellActivity.start(requireActivity(), MyDroidReceiverFragment::class.java)
        }
        binding.sendFileLogicBtn.onClick {
            FragmentShellActivity.start(requireActivity(), ShareReceiverFragment::class.java)
        }
        binding.middleLogicBtn.onClick {

        }

        viewModel.ipData.observe(this) { ip->
            if (ip.isNullOrEmpty()) {
                binding.title.text = "请连接WI-FI或者开启热点"
                if (waitDialog == null) {
                    ConfirmBottomSingleDialog.show(childFragmentManager, "提示",
                        "即将退出，请连接WI-FI或者开启热点，然后重新进入。",
                        "OK",
                        true) { d->
                        waitDialog?.dismissAllowingStateLoss()
                        waitDialog = null
                        requireActivity().finishAfterTransition()
                    }.also { d->
                        d.isCancelable = false
                        waitDialog = d
                    }
                }
            } else {
                binding.title.text = ip
                binding.logicBtnsHost.visible()
                binding.loading.gone()
                waitDialog?.dismissAllowingStateLoss()
                waitDialog = null
            }
        }

        perResult.safeRun({
            ifGotoMgrAll()
        }, notGivePermissionBlock = {
            Toast(requireActivity()).also {
                it.setText("MyDroid: 有一些权限没有授权，后续继续开启吧。")
            }.show()
        })
    }

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

    override fun toolbarInfo() = ToolbarInfo("MyDroid局域网工具")
}