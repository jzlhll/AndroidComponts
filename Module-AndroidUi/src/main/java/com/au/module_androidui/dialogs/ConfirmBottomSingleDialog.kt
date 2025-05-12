package com.au.module_androidui.dialogs

import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.FragmentManager
import com.au.module_android.click.onClick
import com.au.module_android.ui.base.DialogMode
import com.au.module_android.ui.bindings.BindingDialog
import com.au.module_androidui.databinding.ConfirmBottomDialogBinding
import com.au.module_androidui.databinding.ConfirmBottomDialogSingleBinding

open class ConfirmBottomSingleDialog : BindingDialog<ConfirmBottomDialogSingleBinding>(mode = DialogMode.Bottom) {
    companion object {
        fun show(
            manager: FragmentManager,
            title: String?,
            content: String?,
            sureText: String?,
            isWarning: Boolean = false,
            sureClick: Function1<ConfirmBottomSingleDialog, Unit>?,
        ): ConfirmBottomSingleDialog {
            val dialog = ConfirmBottomSingleDialog()
            dialog.onShownBlock = {
                if (isWarning) {
                    //        <item name="android:background">@drawable/corner_btn_warn</item>
                    //        <item name="android:textColor">@color/color_warn_btn_text</item>
                    val warnBg = AppCompatResources.getDrawable(dialog.binding.btn.context, com.au.module_androidcolor.R.drawable.corner_btn_warn)
                    dialog.binding.btn.setBackgroundDrawable(warnBg)
                    val warnTextColor = dialog.binding.btn.context.getColor(com.au.module_androidcolor.R.color.color_warn_btn_text)
                    dialog.binding.btn.setTextColor(warnTextColor)
                }
                dialog.binding.btn.text = sureText
                dialog.binding.btn.onClick { _ ->
                    sureClick?.invoke(dialog)
                }

                dialog.binding.titleTv.text = title
                dialog.binding.contentTv.text = content
            }

            dialog.show(manager, "ConfirmBottomSingleDialog")
            return dialog
        }
    }
}