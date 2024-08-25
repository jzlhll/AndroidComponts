package com.au.module_androidui.dialogs

import androidx.fragment.app.FragmentManager
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingDialog
import com.au.module_androidui.databinding.ConfirmCenterDialogBinding

open class ConfirmCenterDialog : BindingDialog<ConfirmCenterDialogBinding>() {
    companion object {
        fun show(
            manager: FragmentManager,
            title: String?,
            content: String?,
            sureText: String?,
            cancelText:String? = null,
            cancelBlock:Function1<ConfirmCenterDialog, Unit>? = null,
            sureClick: Function1<ConfirmCenterDialog, Unit>?
        ): ConfirmCenterDialog {
            val dialog = ConfirmCenterDialog()
            dialog.onShownBlock = {
                dialog.binding.sureButton.text = sureText
                if (cancelText != null) {
                    dialog.binding.cancelButton.text = cancelText
                }
                dialog.binding.sureButton.onClick {
                    sureClick?.invoke(dialog)
                }

                dialog.binding.titleTv.text = title
                dialog.binding.contentTv.text = content
                dialog.binding.cancelButton.onClick {
                    dialog.dismissAllowingStateLoss()
                    cancelBlock?.invoke(dialog)
                }
            }
            dialog.show(manager, "ConfirmCenterDialog")
            return dialog
        }
    }
}