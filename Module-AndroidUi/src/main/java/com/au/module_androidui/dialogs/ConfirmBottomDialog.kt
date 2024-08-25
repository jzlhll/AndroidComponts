package com.au.module_androidui.dialogs

import androidx.fragment.app.FragmentManager
import com.au.module_android.click.onClick
import com.au.module_android.ui.base.DialogMode
import com.au.module_android.ui.bindings.BindingDialog
import com.au.module_androidui.databinding.ConfirmBottomDialogBinding

open class ConfirmBottomDialog : BindingDialog<ConfirmBottomDialogBinding>(mode = DialogMode.Bottom) {
    companion object {
        fun show(
            manager: FragmentManager,
            title: String?,
            content: String?,
            sureText: String?,
            cancelText:String? = null,
            sureClick: Function1<ConfirmBottomDialog, Unit>?,
        ): ConfirmBottomDialog {
            val dialog = ConfirmBottomDialog()
            dialog.onShownBlock = {
                dialog.binding.sureButton.text = sureText
                if (cancelText != null) {
                    dialog.binding.cancelButton.text = cancelText
                }
                dialog.binding.sureButton.onClick { _ ->
                    sureClick?.invoke(dialog)
                }

                dialog.binding.titleTv.text = title
                dialog.binding.contentTv.text = content
                dialog.binding.cancelButton.onClick { _ ->
                    dialog.dismissAllowingStateLoss()
                }
            }

            dialog.show(manager, "ConfirmDarkAtBottomDialog")
            return dialog
        }
    }
}