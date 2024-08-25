package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import com.au.module_android.fontutil.checkBoldAndSetFont
import com.au.module_android.fontutil.setFontFromAsset
import com.google.android.material.textview.MaterialTextView

/**
 * @author au
 * Date: 2023/8/24
 */
open class CustomFontText : MaterialTextView {
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

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mode = checkBoldAndSetFont(context, attrs)
    }
}