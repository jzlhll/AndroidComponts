package com.au.module_androidex

import androidx.fragment.app.FragmentManager
import com.au.module_android.click.onClick
import com.au.module_android.ui.base.DialogMode
import com.au.module_android.ui.bindings.BindingNorDialog
import com.au.module_androidex.databinding.ConfirmBottomDarkDialogBinding

open class ConfirmDarkAtBottomDialog : BindingNorDialog<ConfirmBottomDarkDialogBinding, ConfirmDarkAtBottomDialog>(mode = DialogMode.Bottom) {
    companion object {
        fun show(
            manager: FragmentManager,
            title: String?,
            content: String?,
            sureText: String?,
            cancelText:String? = null,
            sureClick: Function1<ConfirmDarkAtBottomDialog, Unit>?,
        ): ConfirmDarkAtBottomDialog {
            val dialog = ConfirmDarkAtBottomDialog()
            dialog.onShownBlock = {
                val d = it as ConfirmDarkAtBottomDialog
                d.binding.sureButton.text = sureText
                if (cancelText != null) {
                    d.binding.cancelButton.text = cancelText
                }
                d.binding.sureButton.onClick {
                    sureClick?.invoke(d)
                }

                d.binding.titleTv.text = title
                d.binding.contentTv.text = content
                d.binding.cancelButton.onClick {
                    d.dismissAllowingStateLoss()
                }
            }

            dialog.show(manager, "StronglyBottomDarkDialogV2")
            return dialog
        }
    }
}