package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import com.au.module.android.R
import com.au.module_android.utils.viewBackgroundBuild

/**
 * @author allan
 * @date :2024/3/12 14:46
 * @description:
 */
class BgBuildView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {
    init {
        val attr = context.obtainStyledAttributes(attrs, R.styleable.AnySimpleView)
        viewBackgroundBuild(attr)
        attr.recycle()
    }
}