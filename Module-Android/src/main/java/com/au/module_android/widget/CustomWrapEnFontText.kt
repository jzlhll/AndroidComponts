package com.au.module_android.widget

import android.content.Context
import android.text.Layout
import android.util.AttributeSet
import com.au.module_android.fontutil.checkBoldAndSetFont
import com.au.module_android.fontutil.setFontFromAsset
import com.google.android.material.textview.MaterialTextView
import kotlin.math.ceil

/**
 * @author au
 * Date: 2023/8/24
 */
open class CustomWrapEnFontText : MaterialTextView {
    private var mode: TextViewCheckMode

    var fontMode: FontMode
        get() {
            return mode.fontMode
        }
        set(value) {
            mode.fontMode = value
            setFontFromAsset(context, mode.fontMode, mode.isNumber)
            invalidate()
        }

    var isNumber:Boolean
        get() {
            return mode.isNumber
        }
        set(value) {
            mode.isNumber = value
            setFontFromAsset(context, mode.fontMode, mode.isNumber)
            invalidate()
        }

    constructor(context: Context) : super(context) {
        mode = TextViewCheckMode(FontMode.NORMAL, false)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mode = checkBoldAndSetFont(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mode = checkBoldAndSetFont(context, attrs)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (fixWrapEndToolLong) {
            val width = ceil(getMaxLineWidth(layout))
            val height = measuredHeight
            setMeasuredDimension(width.toInt(), height)
        }
    }

    var fixWrapEndToolLong = false

    private fun getMaxLineWidth(layout: Layout) : Float {
        var maximumWidth = 0.0f
        val lines = layout.lineCount
        var i = 0
        while (i < lines) {
            maximumWidth = Math.max(layout.getLineWidth(i), maximumWidth)
            i++
        }

        return maximumWidth
    }
}