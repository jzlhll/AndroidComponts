package com.allan.androidlearning.transfer.views

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.allan.androidlearning.R
import com.allan.androidlearning.databinding.FragmentMyDroidAllBinding
import com.allan.androidlearning.transfer.views.receiver.MyDroidReceiverFragment
import com.allan.androidlearning.transfer.views.send.SendListSelectorFragment
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.permissions.PermissionStorageHelper
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.gone
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmBottomSingleDialog
import com.au.module_androidui.dialogs.ConfirmCenterDialog

@EntryFrgName(priority = 12)
class MyDroidAllFragment : BindingFragment<FragmentMyDroidAllBinding>() {

    var waitDialog:ConfirmBottomSingleDialog? = null

    val viewModel by unsafeLazy {
        ViewModelProvider(this)[MyDroidAllViewModel::class.java]
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.receiveFileLogicBtn.onClick {
            FragmentShellActivity.start(requireActivity(), MyDroidReceiverFragment::class.java)
        }
        binding.sendFileLogicBtn.onClick {
            FragmentShellActivity.start(requireActivity(), SendListSelectorFragment::class.java)
        }
        binding.middleLogicBtn.onClick {

        }

        viewModel.ipData.observe(this) { ip->
            if (ip.isNullOrEmpty()) {
                binding.title.setText(R.string.connect_wifi_or_hotspot)
                if (waitDialog == null) {
                    ConfirmBottomSingleDialog.show(childFragmentManager, getString(R.string.tips),
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

        val helper = PermissionStorageHelper()
        helper.ifGotoMgrAll {
            ConfirmCenterDialog.Companion.show(childFragmentManager,
                getString(R.string.app_management_permission),
                getString(R.string.global_permission_prompt),
                "OK") {
                helper.gotoMgrAll(requireActivity())
                it.dismissAllowingStateLoss()
            }
        }
    }

    override fun toolbarInfo() = ToolbarInfo("MyDroid局域网工具")
}