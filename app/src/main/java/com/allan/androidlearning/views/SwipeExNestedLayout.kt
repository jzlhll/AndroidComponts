package com.allan.androidlearning.views

import android.content.Context
import android.util.AttributeSet
import androidx.core.view.forEach

/**
 *
 * 描述：
 *
 * @author jiale.wei
 *
 * 创建时间：2022/3/31 13:57
 *
 */
open class SwipeExNestedLayout : SwipeNestedLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /**
     * 实际滑动距离比例
     */
    open var offsetRatio = 0.4f

    init {
        isNestedScrollingEnabled = true
    }


    override fun onNotifyOffsetChange(realOffsetX: Int, realOffsetY: Int, isAnim: Boolean) {
        if (onNotifyOffsetChangeHoldUp?.invoke(realOffsetX, realOffsetY, isAnim) != true) {
            forEach {
                it.translationY = -realOffsetY * offsetRatio
                it.translationX = -realOffsetX * offsetRatio
            }
        }
    }
}