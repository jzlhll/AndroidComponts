package com.au.module_androiduidark.widget

import android.content.Context
import android.util.AttributeSet
import com.au.module_androidex.toolbar.AbsFakeToolbar
import com.au.module_androiduidark.R

/**
 * @date :2024/4/28 15:54
 * @description:
 */
class DarkFakeToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsFakeToolbar(context, attrs) {
    override fun layoutRId() = R.layout.fake_tool_bar_dark

    override fun titleRId() = R.id.fakeToolbarTitle

    override fun backRId() = R.id.fakeToolbarBack

    override fun defaultBackImage() = R.drawable.icon_back_dark
}