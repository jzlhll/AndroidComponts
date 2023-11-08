package com.allan.androidlearning

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import com.au.module_android.ui.base.BaseCenterDialog

/**
 * @author allan
 * @date :2023/11/8 14:20
 * @description:
 */
class MyDialog : BaseCenterDialog() {
    override fun onCreateContentView(inflater: LayoutInflater, savedInstanceState: Bundle?): View {
        return LinearLayout(inflater.context).also {
            it.setBackgroundColor(Color.CYAN)
            it.layoutParams = LinearLayout.LayoutParams(500, 300)
        }
    }
}