package com.allan.mydroid.views.send

import android.os.Bundle
import androidx.core.view.isVisible
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.databinding.DialogMyDroidSendlistBinding
import com.au.module_android.click.onClick
import com.au.module_android.ui.base.findDialog
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

class SendListSelectorDialog : BindingFragment<DialogMyDroidSendlistBinding>() {
    interface ISelectItemClick {
        fun onItemClick(bean: UriRealInfoEx)
    }

    private val common = object : SendListSelectorCommon(this) {
        override fun rcv() = binding.rcv
        override fun empty() = binding.empty

        override fun itemClick(bean: UriRealInfoEx?) {
            bean ?: return
            var parent = parentFragment as? ISelectItemClick
            if (parent == null) {
                parent = parentFragment?.parentFragment as? ISelectItemClick
            }
            if (parent == null) {
                parent = parentFragment?.parentFragment?.parentFragment as? ISelectItemClick
            }
            parent?.onItemClick(bean)
            findDialog(this@SendListSelectorDialog)?.dismissAllowingStateLoss()
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        common.onBindingCreated()
        binding.infoText.onClick {
            if (binding.disclaimerTv.isVisible) {
                binding.disclaimerTv.gone()
                binding.rcv.visible()
                if(common.isEmptyList()) binding.empty.visible() else binding.empty.gone()
            } else {
                binding.disclaimerTv.visible()
                binding.rcv.gone()
                binding.empty.gone()
            }
        }

    }
}