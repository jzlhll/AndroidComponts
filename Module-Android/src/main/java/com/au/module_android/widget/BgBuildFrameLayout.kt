package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.au.module_android.R
import com.au.module_android.utils.viewBackgroundBuild
import androidx.core.content.withStyledAttributes

/**
 * @author allan
 * @date :2024/3/12 14:46
 * @description:
 */
class BgBuildFrameLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    init {
        context.withStyledAttributes(attrs, R.styleable.AnyView) {
            viewBackgroundBuild(this)
        }
    }


}