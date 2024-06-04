package com.allan.autoclickfloat.floats.views

import android.content.Intent
import android.widget.TextView
import com.allan.autoclickfloat.AllPermissionActivity
import com.allan.autoclickfloat.R
import com.au.module_android.Globals
import com.au.module_android.utils.startActivityFix

class FloatingStepView : BaseFloatingView( R.layout.view_floating_step) {
    private var stepIndexTv :TextView? = null

    init {
        stepIndexTv = mRoot.findViewById(R.id.stepIndexTv)
    }

    fun updateStepIndex(index:Int) {
        stepIndexTv?.text = "$index"
    }
}

class FloatingSettingView : BaseFloatingView(R.layout.view_floating_setting) {
    init {
        clickCallback = {
            Globals.app.startActivityFix(Intent(Globals.app, AllPermissionActivity::class.java))
        }
    }
}