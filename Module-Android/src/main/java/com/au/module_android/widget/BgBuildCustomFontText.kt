package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import com.au.module_android.R
import com.au.module_android.utils.viewBackgroundBuild
import androidx.core.content.withStyledAttributes

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
        context.withStyledAttributes(attrs, R.styleable.AnyView) {
            viewBackgroundBuild(this)
        }
    }
}