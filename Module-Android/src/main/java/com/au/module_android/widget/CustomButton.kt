package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import com.au.module.android.R
import com.au.module_android.utils.viewBackgroundBuild

/**
 * @author au
 * @date :2023/11/7 15:37
 * @description:
 */
class CustomButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : CustomFontText(context, attrs) {
    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.AnySimpleView)
        viewBackgroundBuild(attr)
        attr.recycle()
    }
}