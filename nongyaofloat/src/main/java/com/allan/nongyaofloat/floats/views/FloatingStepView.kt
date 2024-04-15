package com.allan.nongyaofloat.floats.views

import android.widget.TextView
import com.allan.nongyaofloat.R

class FloatingStepView : BaseFloatingView(R.layout.view_floating_step) {
    private var stepIndexTv :TextView? = null

    init {
        stepIndexTv = mRoot.findViewById(R.id.stepIndexTv)
    }

    fun updateStepIndex(index:Int) {
        stepIndexTv?.text = "$index"
    }
}