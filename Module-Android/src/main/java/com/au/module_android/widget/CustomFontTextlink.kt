package com.au.module_android.widget

import android.content.Context
import android.graphics.Paint
import android.util.AttributeSet

/**
 * @author au
 * @date :2023/11/7 15:41
 * @description:
 */
open class CustomFontTextlink : CustomFontText {
    constructor(context: Context) : super(context)
    //改动代码，init的不规范和没有必要的二次设置font
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        paint.flags = Paint.UNDERLINE_TEXT_FLAG //下划线
        paint.isAntiAlias = true//抗锯齿
    }
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        paint.flags = Paint.UNDERLINE_TEXT_FLAG //下划线
        paint.isAntiAlias = true//抗锯齿
    }
}