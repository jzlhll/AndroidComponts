package com.allan.androidlearning.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.allan.androidlearning.MyDialog
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
            text = "showDialog"
            onClick {
                val dialog = MyDialog()
                dialog.show(this@DialogsFragment.childFragmentManager, "MyDialog#12")
            }
        }
        ll.addView(dialog1Btn)

        return ll
    }
}