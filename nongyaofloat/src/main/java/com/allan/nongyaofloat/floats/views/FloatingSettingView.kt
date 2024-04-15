package com.allan.nongyaofloat.floats.views

import android.view.View
import com.allan.nongyaofloat.R
import com.allan.nongyaofloat.floats.views.BaseFloatingView
import com.au.module_android.click.onClick

class FloatingSettingView : BaseFloatingView(R.layout.view_floating_setting) {
    private var saveBtn :View? = null
    init {
        saveBtn = mRoot.findViewById(R.id.saveBtn)
        saveBtn?.onClick {

        }
    }
}