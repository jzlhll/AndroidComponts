package com.au.module_android.text

import android.content.Context
import android.util.AttributeSet
import com.google.android.material.textview.MaterialTextView

/**
 * @author allan.jiang
 * Date: 2023/8/24
 */
class CustomTextView : MaterialTextView {
    private var mode:TextViewCheckMode

    var fontMode:FontMode
        get() {
            return mode.fontSizeMode
        }
        set(value) {
            mode.fontSizeMode = value
            setFontFromAsset(context, mode.fontSizeMode, mode.isNumber)
            invalidate()
        }

    var isNumber:Boolean
        get() {
            return mode.isNumber
        }
        set(value) {
            mode.isNumber = value
            setFontFromAsset(context, mode.fontSizeMode, mode.isNumber)
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
}