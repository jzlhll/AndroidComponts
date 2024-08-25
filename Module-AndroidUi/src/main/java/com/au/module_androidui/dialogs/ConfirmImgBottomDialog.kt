package com.au.module_androidui.dialogs

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentManager
import com.au.module_android.click.onClick
import com.au.module_android.ui.base.DialogMode
import com.au.module_android.ui.bindings.BindingDialog
import com.au.module_androidui.databinding.ConfirmBottomImgDialogBinding

open class ConfirmImgBottomDialog : BindingDialog<ConfirmBottomImgDialogBinding>(mode = DialogMode.Bottom) {
    companion object {
        fun show(
            manager: FragmentManager,
            imageRes:Int,
            @ColorInt imageTint:Int?,
            title: String?,
            content: String?,
            sureText: String?,
            cancelText:String? = null,
            sureClick: Function1<ConfirmImgBottomDialog, Unit>?,
        ): ConfirmImgBottomDialog {
            val dialog = ConfirmImgBottomDialog()
            dialog.onShownBlock = {
                dialog.binding.icon.setImageResource(imageRes)
                if (imageTint != null) {
                    dialog.binding.icon.imageTintList = ColorStateList.valueOf(imageTint)
                }

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
                }
            }
            dialog.show(manager, "ConfirmDarkImgAtBottomDialog")
            return dialog
        }
    }
}