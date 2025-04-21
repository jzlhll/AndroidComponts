package com.au.logsystem

import android.content.Context
import android.util.AttributeSet
import android.view.animation.LinearInterpolator
import androidx.annotation.Keep
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton

@Keep
class FabScrollBehavior : AbsFabBehavior {
    constructor() :super()
    constructor(context: Context, attrs: AttributeSet):super(context, attrs)

    override fun show(child: FloatingActionButton) {
        child.animate().translationY(0f).setInterpolator(LinearInterpolator()).start()
    }

    override fun hide(child: FloatingActionButton) {
        val layoutParams = child.layoutParams as CoordinatorLayout.LayoutParams
        val bottomMargin = layoutParams.bottomMargin
        child.animate().translationY((child.height + bottomMargin).toFloat()).setInterpolator(LinearInterpolator()).start()
    }

    override val minTimeForHide: Int
        get() = 2000
}