package com.au.module_androidex

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentManager
import com.au.module_android.click.onClick
import com.au.module_android.ui.base.DialogMode
import com.au.module_android.ui.bindings.BindingNorDialog
import com.au.module_androidex.databinding.ConfirmBottomDarkImgDialogBinding

open class ConfirmDarkImgAtBottomDialog : BindingNorDialog<ConfirmBottomDarkImgDialogBinding, ConfirmDarkImgAtBottomDialog>(mode = DialogMode.Bottom) {
    companion object {
        fun show(
            manager: FragmentManager,
            imageRes:Int,
            @ColorInt imageTint:Int?,
            title: String?,
            content: String?,
            sureText: String?,
            cancelText:String? = null,
            sureClick: Function1<ConfirmDarkImgAtBottomDialog, Unit>?,
        ): ConfirmDarkImgAtBottomDialog {
            val dialog = ConfirmDarkImgAtBottomDialog()
            dialog.onShownBlock = {
                val d = it as ConfirmDarkImgAtBottomDialog
                d.binding.icon.setImageResource(imageRes)
                if (imageTint != null) {
                    d.binding.icon.imageTintList = ColorStateList.valueOf(imageTint)
                }

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
            dialog.show(manager, "StronglyBottomDarkImageDialogV2")
            return dialog
        }
    }
}