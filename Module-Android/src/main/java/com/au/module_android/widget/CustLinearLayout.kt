package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.au.module.android.R
import com.au.module_android.ui.viewBackgroundBuild

/**
 * @author allan
 * @date :2024/3/12 14:46
 * @description:
 */
class CustLinearLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : LinearLayout(context, attrs) {
    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.AnySimpleView)
        viewBackgroundBuild(attr)
        attr.recycle()
    }
}