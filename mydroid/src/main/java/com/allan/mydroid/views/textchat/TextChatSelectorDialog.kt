package com.allan.mydroid.views.textchat

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.allan.mydroid.databinding.TextchatSelectorDialogBinding
import com.allan.mydroid.views.MyDroidAllFragment
import com.au.module_android.click.onClick
import com.au.module_android.ui.FragmentShellActivity
import com.au.module_android.ui.base.findDialog
import com.au.module_android.ui.bindings.BindingFragment
import com.au.module_android.utils.asOrNull
import com.au.module_androidui.dialogs.FragmentBottomSheetDialog

class TextChatSelectorDialog : BindingFragment<TextchatSelectorDialogBinding>() {
    companion object {
        fun show(f: Fragment) {
            FragmentBottomSheetDialog.show<TextChatSelectorDialog>(f.childFragmentManager)
        }
    }

    override fun onBindingCreated(savedInstanceState: Bundle?) {
        binding.asClientHost.onClick {
            parentFragment?.parentFragment?.asOrNull<MyDroidAllFragment>()?.runCheckIp {
                FragmentShellActivity.start(requireActivity(), TextChatClientFragment::class.java)
            }
            findDialog(parentFragment)?.dismissAllowingStateLoss()
        }
        binding.asServerHost.onClick {
            parentFragment?.parentFragment?.asOrNull<MyDroidAllFragment>()?.runCheckIp {
                FragmentShellActivity.start(requireActivity(), TextChatServerFragment::class.java)
            }
            findDialog(parentFragment)?.dismissAllowingStateLoss()
        }
        binding.cancelHost.onClick {
            findDialog(parentFragment)?.dismissAllowingStateLoss()
        }
    }
}