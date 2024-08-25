package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import com.au.module_android.fontutil.checkBoldAndSetFont
import com.google.android.material.textfield.TextInputEditText

/**
 * @author au
 * @date :2023/11/7 15:42
 * @description:
 */
class CustomEditText : TextInputEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        checkBoldAndSetFont(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        checkBoldAndSetFont(context, attrs)
    }

    private var onSelectionChanged:((edit: CustomEditText, selStart:Int, selEnd:Int)->Unit)? = null

    fun doOnSelectionChanged(change:(edit: CustomEditText, selStart:Int, selEnd:Int)->Unit) {
        onSelectionChanged = change
    }

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        super.onSelectionChanged(selStart, selEnd)
        onSelectionChanged?.invoke(this, selStart, selEnd)
    }
}