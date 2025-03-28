package com.au.module_android.widget

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import com.au.module_android.R
import com.au.module_android.utils.viewBackgroundBuild
import androidx.core.content.withStyledAttributes

/**
 * @author allan
 * @date :2024/3/13 15:21
 * @description:
 */
class BgBuildConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {
    init {
        context.withStyledAttributes(attrs, R.styleable.AnyView) {
            viewBackgroundBuild(this)
        }
    }
}