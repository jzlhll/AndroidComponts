package com.au.module_android.widget

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.widget.ScrollView
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.getScreenFullSize

/**
 * 通过两种方式，来限定ScrollView的最大高度。
 * 使用场景：
 */
class LimitMaxHeightScrollView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ScrollView(context, attrs) {
    /**
     * 两种模式之一：直接设置本ScrollView的高度限定
     */
    var limitMaxHeight:Int? = null

    /**
     * 两种模式之二：设定整个屏幕高度抛出的高度，剩余是最大的高度限定。
     */
    var limitCutOffHeight:Int? = null

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val limitMH = limitMaxHeight
        val limitCutOffH = limitCutOffHeight
        if (limitMH != null && limitMH > 0) {
            val mh = measuredHeight
            if (mh > limitMH) {
                setMeasuredDimension(measuredWidth, limitMH)
            }
        } else if (limitCutOffH != null && limitCutOffH > 0) {
            val screenSize = context.asOrNull<Activity>()?.getScreenFullSize()
            val mh = measuredHeight
            if (screenSize != null) {
                val max = screenSize.second - limitCutOffH
                if (mh > max) {
                    setMeasuredDimension(measuredWidth, max)
                }
            }
        }
    }
}