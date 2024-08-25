package com.au.module_androidui.widget

import android.content.Context
import android.util.AttributeSet
import com.au.module_android.ui.toolbar.AbsFakeToolbar
import com.au.module_androidui.R

/**
 * @author allan
 * @date :2024/4/28 15:56
 * @description:
 */
class FakeToolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AbsFakeToolbar(context, attrs) {
    override fun layoutRId() = R.layout.fake_tool_bar

    override fun titleRId() = R.id.fakeToolbarTitle

    override fun backRId() = R.id.fakeToolbarBack

    override fun defaultBackImage() = com.au.module_androidcolor.R.drawable.icon_back
}