package com.allan.mydroid.views.textchat

import android.os.Bundle
import com.allan.mydroid.databinding.TextchatIpportInputDialogBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.base.findDialog
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_cached.delegate.SharedPrefStringCache

class TextChatClientIpPortInputDialog : BindingFragment<TextchatIpportInputDialogBinding>() {
    private var ipCache by SharedPrefStringCache("textchat_server_ip", "")
    private var portCache by SharedPrefStringCache("textchat_server_port", "")

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.ipInput.setText(ipCache)
        binding.portInput.setText(portCache)

        binding.sureBtn.onClick {
            if(binding.ipInput.text.isNullOrEmpty() || binding.portInput.text.isNullOrEmpty()) {
                return@onClick
            }

            ipCache = binding.ipInput.text.toString()
            portCache = binding.portInput.text.toString()

            parentFragment?.parentFragment?.asOrNull<TextChatClientFragment>()?.connectServer(
                ipCache, portCache.toInt()
            )

            findDialog(parentFragment)?.dismissAllowingStateLoss()
        }

        binding.cancelBtn.onClick {
            parentFragment?.parentFragment?.requireActivity()?.finishAfterTransition()
        }
    }
}