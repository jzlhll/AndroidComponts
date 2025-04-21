package com.au.logsystem

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.Keep
import com.google.android.material.floatingactionbutton.FloatingActionButton

@Keep
class FabHideBehavior : AbsFabBehavior {
    constructor() :super()
    constructor(context: Context, attrs: AttributeSet):super(context, attrs)

    override fun show(child: FloatingActionButton) {
        child.show()
    }

    override fun hide(child: FloatingActionButton) {
        child.hide()
    }

    override val minTimeForHide: Int
        get() = 1500
}