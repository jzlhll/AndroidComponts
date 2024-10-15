package com.allan.androidlearning.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allan.classnameanno.EntryFrgName
import com.au.module_android.click.onClick
import com.au.module_android.ui.views.ViewFragment
import com.au.module_android.widget.FlowLayout
import com.au.module_androidui.dialogs.ConfirmCenterDialog
import com.google.android.material.button.MaterialButton

/**
 * @author au
 * @date :2023/11/8 14:16
 * @description:
 */
@EntryFrgName
class DialogsFragment : ViewFragment() {
    override fun onUiCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = inflater.context
        val ll = FlowLayout(context)
        ll.addView(MaterialButton(context).apply {
            text = "showViewDialog"
            onClick {
                val dialog = MyCenterDialog()
                dialog.show(this@DialogsFragment.childFragmentManager, "MyDialog#12")
            }
        })

        ll.addView(MaterialButton(context).apply {
            text = "showBindingDialog"
            onClick {
                val dialog = MyBottomDialog()
                dialog.show(this@DialogsFragment.childFragmentManager, "MyDialog#12")
            }
        })

        ll.addView(MaterialButton(context).apply {
            text = "ConfirmCenterDialog"
            onClick {
                ConfirmCenterDialog.show(childFragmentManager, "Hello", "This is a small content.", "OK") {}
            }
        })
        return ll
    }
}