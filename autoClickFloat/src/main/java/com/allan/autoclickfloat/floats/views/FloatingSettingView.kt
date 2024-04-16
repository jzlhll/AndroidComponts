package com.allan.autoclickfloat.floats.views

import android.view.View
import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.floats.views.BaseFloatingView
import com.au.module_android.click.onClick

class FloatingSettingView : BaseFloatingView(R.layout.view_floating_setting) {
    private var saveBtn :View? = null
    init {
        saveBtn = mRoot.findViewById(R.id.saveBtn)
        saveBtn?.onClick {

        }
    }
}