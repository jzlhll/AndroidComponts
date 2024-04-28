package com.au.module_android.widget

import android.content.Context
import android.graphics.Paint.FontMetricsInt
import android.graphics.Rect
import android.text.Layout
import android.util.AttributeSet
import com.au.module.android.R
import com.au.module_android.ui.viewBackgroundBuild
import com.google.android.material.textview.MaterialTextView
import kotlin.math.ceil

/**
 * @author au
 * Date: 2023/8/24
 */
open class BgBuildCustomFontText : CustomFontText {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.AnySimpleView)
        viewBackgroundBuild(attr)
        attr.recycle()
    }
}