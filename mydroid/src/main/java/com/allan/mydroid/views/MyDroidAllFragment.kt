package com.allan.mydroid.views

import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.allan.mydroid.views.receiver.MyDroidReceiverFragment
import com.allan.mydroid.views.send.SendListSelectorFragment
import com.allan.mydroid.databinding.FragmentMyDroidAllBinding
import com.allan.mydroid.globals.MY_DROID_SHARE_IMPORT_URIS
import com.au.module_android.click.onClick
import com.au.module_android.permissions.PermissionStorageHelper
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.gone
import com.au.module_android.utils.logdNoFile
import com.au.module_android.utils.unsafeLazy
import com.au.module_android.utils.visible
import com.au.module_androidui.dialogs.ConfirmBottomSingleDialog
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.allan.mydroid.R

class MyDroidAllFragment : BindingFragment<FragmentMyDroidAllBinding>() {

    var waitDialog:ConfirmBottomSingleDialog? = null

    val viewModel by unsafeLazy {
        ViewModelProvider(this)[MyDroidAllViewModel::class.java]
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.textChatServerBtn.onClick {

        }
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
                        getString(R.string.exit_with_wifi_reminder),
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

    override fun onResume() {
        super.onResume()
        parseShareImportIntent()
    }

    private fun parseShareImportIntent() {
        val isFromNewShareImportUris = arguments?.getString(MY_DROID_SHARE_IMPORT_URIS)
        arguments?.remove(MY_DROID_SHARE_IMPORT_URIS)
        logdNoFile { "parse ShareImport Intent $isFromNewShareImportUris" }
        if (isFromNewShareImportUris == MY_DROID_SHARE_IMPORT_URIS) {
            lifecycleScope.launch {
                delay(200)
                FragmentShellActivity.start(requireActivity(), SendListSelectorFragment::class.java)
            }
        }
    }

    override fun toolbarInfo() = ToolbarInfo(getString(R.string.app_name_lan_tool), hasBackIcon = false)
}