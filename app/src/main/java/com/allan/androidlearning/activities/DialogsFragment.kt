package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.allan.androidlearning.dialogs.MyBottomDialog
import com.allan.androidlearning.dialogs.MyCenterDialog
import com.au.module_android.click.onClick
import com.au.module_android.ui.views.ViewFragment
import com.google.android.material.button.MaterialButton

/**
 * @author allan
 * @date :2023/11/8 14:16
 * @description:
 */
class DialogsFragment : ViewFragment() {
    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = inflater.context
        val ll = LinearLayout(context)
        val dialog1Btn = MaterialButton(context).apply {
            text = "showViewDialog"
            onClick {
                val dialog = MyCenterDialog()
                dialog.show(this@DialogsFragment.childFragmentManager, "MyDialog#12")
            }
        }
        ll.addView(dialog1Btn)

        val dialog2Btn = MaterialButton(context).apply {
            text = "showBindingDialog"
            onClick {
                val dialog = MyBottomDialog()
                dialog.show(this@DialogsFragment.childFragmentManager, "MyDialog#12")
            }
        }
        ll.addView(dialog2Btn)

        return ll
    }
}