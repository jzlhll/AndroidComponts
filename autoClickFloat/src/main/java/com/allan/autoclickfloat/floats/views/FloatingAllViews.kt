package com.allan.autoclickfloat.floats.views

import android.content.Intent
import android.widget.TextView
import com.allan.autoclickfloat.AllPermissionActivity
import com.allan.autoclickfloat.activities.autooneclick.AutoContinuousClickActivityFragment
import com.allan.autoclickfloat.R
import com.au.module_android.Globals
import com.au.module_android.utils.startActivityFix

class FloatingStepView : BaseFloatingView("FloatingStepView", R.layout.view_floating_step) {
    private var stepIndexTv :TextView? = null

    init {
        stepIndexTv = mRoot.findViewById(R.id.stepIndexTv)
    }

    fun updateStepIndex(index:Int) {
        stepIndexTv?.text = "$index"
    }
}

class FloatingSettingView : BaseFloatingView("FloatingSettingView", R.layout.view_floating_setting) {
    init {
        clickCallback = {
            Globals.app.startActivityFix(Intent(Globals.app, AllPermissionActivity::class.java))
        }
    }
}