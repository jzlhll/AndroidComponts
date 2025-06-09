package com.allan.mydroid.views

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.allan.mydroid.R
import com.allan.mydroid.databinding.FragmentMyDroidAllBinding
import com.allan.mydroid.globals.MY_DROID_SHARE_IMPORT_URIS
import com.allan.mydroid.globals.SimpleNetworkObserver
import com.allan.mydroid.views.receiver.MyDroidReceiverFragment
import com.allan.mydroid.views.send.SendListSelectorFragment
import com.allan.mydroid.views.textchat.TextChatSelectorDialog
import com.au.module_android.click.onClick
import com.au.module_android.permissions.PermissionStorageHelper
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.ui.views.ToolbarInfo
import com.au.module_android.utils.logdNoFile
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.au.module_androidui.toast.ToastBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MyDroidAllFragment : BindingFragment<FragmentMyDroidAllBinding>() {
    private var mIp:String? = null
    private val netObserver = object : SimpleNetworkObserver() {
        public override fun netRegister() {
            super.netRegister()
        }
        public override fun netUnregister() {
            super.netUnregister()
        }
    }.apply {
        onChanged = { ip->
            mIp = ip
            lifecycleScope.launch {
                val curIp = mIp
                if (!curIp.isNullOrEmpty()) {
                    binding.title.text = curIp
                } else {
                    binding.title.setText(R.string.connect_wifi_or_hotspot)
                }
            }
        }
    }

    fun runCheckIp(workBlock:()->Unit) {
        if (!mIp.isNullOrEmpty()) {
            workBlock()
        } else {
            ToastBuilder().setMessage(getString(R.string.connect_wifi_or_hotspot))
                .setOnTop()
                .toast()
        }
    }

    override fun onStart() {
        super.onStart()
        netObserver.netRegister()
    }

    override fun onStop() {
        super.onStop()
        netObserver.netUnregister()
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.textChatBtn.onClick {
            runCheckIp {
                TextChatSelectorDialog.show(this)
            }
        }
        binding.receiveFileLogicBtn.onClick {
            runCheckIp {
                FragmentShellActivity.start(requireActivity(), MyDroidReceiverFragment::class.java)
            }
        }
        binding.sendFileLogicBtn.onClick {
            runCheckIp {
                FragmentShellActivity.start(requireActivity(), SendListSelectorFragment::class.java)
            }
        }
        binding.middleLogicBtn.onClick {
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

    override fun toolbarInfo() = ToolbarInfo(getString(R.string.app_name), hasBackIcon = false)
}