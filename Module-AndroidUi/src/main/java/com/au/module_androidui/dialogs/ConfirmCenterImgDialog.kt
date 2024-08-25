package com.au.module_androidui.dialogs

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentManager
import com.au.module_android.click.onClick
import com.au.module_android.ui.bindings.BindingDialog
import com.au.module_androidui.databinding.ConfirmCenterImgDialogBinding

open class ConfirmCenterImgDialog : BindingDialog<ConfirmCenterImgDialogBinding>() {
    companion object {
        fun show(
            manager: FragmentManager,
            imageRes:Int,
            @ColorInt imageTint:Int?,
            title: String?,
            content: String?,
            sureText: String?,
            cancelText:String? = null,
            sureClick: Function1<ConfirmCenterImgDialog, Unit>?,
        ): ConfirmCenterImgDialog {
            val dialog = ConfirmCenterImgDialog()
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

            dialog.show(manager, "ConfirmCenterImgDialog")
            return dialog
        }
    }
}