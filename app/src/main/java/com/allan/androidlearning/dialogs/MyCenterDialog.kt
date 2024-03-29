package com.allan.androidlearning.dialogs

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.au.module_android.ui.base.AbsDialog
import com.au.module_android.ui.base.DialogMode

/**
 * @author au
 * @date :2023/11/8 14:20
 * @description:
 */
class MyCenterDialog : AbsDialog(mode=DialogMode.Center) {
    override fun onCreatingView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return LinearLayout(inflater.context).also {
            it.setBackgroundColor(Color.CYAN)
            it.layoutParams = LinearLayout.LayoutParams(500, 300)
        }
    }
}