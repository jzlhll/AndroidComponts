package com.allan.androidlearning.views


import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.view.ViewCompat
import androidx.core.view.forEach

/**
 * 只是对[BaseNestedScrollingParentLayout]的一个是用案例
 * 根据需求自己封装
 */
open class SwipeNestedLayout : RefreshConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    /***
     * 当前view的偏移量
     */
    private var realOffsetY = 0
    private var realOffsetX = 0

    /**
     * 位移动画需要记录的临时变量
     */
    private var animEndX = 0
    private var animEndY = 0
    private var animOffX = 0
    private var animOffY = 0

    private val stopAnim by lazy {
        ValueAnimator.ofInt(0, 0).apply {
            doOnStart {
                animOffX = realOffsetX - animEndX
                animOffY = realOffsetY - animEndY
            }
            addUpdateListener {
                val f = 1 - it.animatedFraction
                realOffsetX = (animOffX * f + animEndX).toInt()
                realOffsetY = (animOffY * f + animEndY).toInt()
                onNotifyOffsetChange(realOffsetX, realOffsetY, true)
            }
            doOnEnd {
                endAction?.invoke(this@SwipeNestedLayout)
                endAction = null
            }
        }
    }


    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if (stopAnim.isRunning) {
            stopAnim.cancel()
        }
        if (type != ViewCompat.TYPE_TOUCH) {
            return false
        }
        return super.onStartNestedScroll(child, target, axes, type)
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        super.onNestedPreScroll(target, dx, dy, consumed, type)
        val newRealOffsetX = checkPreScroll(realOffsetX, dx, consumed, 0)
        val newRealOffsetY = checkPreScroll(realOffsetY, dy, consumed, 1)
        if (newRealOffsetX != realOffsetX || newRealOffsetY != realOffsetY) {
            realOffsetX = newRealOffsetX
            realOffsetY = newRealOffsetY
            onNotifyOffsetChange(realOffsetX, realOffsetY, false)
        }
    }

    private fun checkPreScroll(
        oldRealOffset: Int,
        offSet: Int,
        consumed: IntArray,
        index: Int
    ): Int {
        if ((index == 0 && !enableHorizontal) || (index == 1 && !enableVertical)) {
            return 0
        }
        if (offSet == 0 || oldRealOffset == 0) {
            return oldRealOffset
        }
        //realOffset不为0，判断发生了位移
        var newRealOffset = oldRealOffset
        when {
            oldRealOffset > 0 -> {
                val off = newRealOffset + offSet
                if (off > 0) {//需要消耗完所有滚动距离
                    consumed[index] = offSet
                    newRealOffset += offSet
                } else {//只需要消耗realOffset的值
                    consumed[index] = -oldRealOffset
                    newRealOffset = 0
                }
            }
            oldRealOffset < 0 -> {
                val off = newRealOffset + offSet
                if (off < 0) {//需要消耗完所有滚动距离
                    consumed[index] = offSet
                    newRealOffset += offSet
                } else {//只需要消耗realOffset的值
                    consumed[index] = -oldRealOffset
                    newRealOffset = 0
                }
            }
        }
        return newRealOffset
    }

    override fun onNestedScroll(
        target: View/*滚动的子view*/,
        dxConsumed: Int/*子view已经消耗*/,
        dyConsumed: Int/*子view已经消耗*/,
        dxUnconsumed: Int/*子view未消耗*/,
        dyUnconsumed: Int/*子view未消耗*/,
        type: Int,
        consumed: IntArray
    ) {
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
        val oldX = realOffsetX
        val oldY = realOffsetX
        if (enableHorizontal) {
            realOffsetX += dxUnconsumed
        }
        if (enableVertical) {
            realOffsetY += dyUnconsumed
        }
        if (oldX != realOffsetX || oldY != realOffsetY) {
            onNotifyOffsetChange(realOffsetX, realOffsetY, false)
        }
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        super.onStopNestedScroll(target, type)
        if (type == ViewCompat.TYPE_TOUCH) {
            animReset()
        }
    }
    /*******************以下是对外接口*****************************/
    /**
     * 重置时x的位置
     */
    var resetX = 0

    /**
     * 重置时Y的位置
     */
    var resetY = 0

    /**
     * 重置时动画时长
     */
    var resetAnimDuration = 230L

    /**
     * 是否允许监听水平
     */
    var enableHorizontal: Boolean = true

    /**
     * 是否允许监听竖直
     */
    var enableVertical: Boolean = true

    /**
     * 动画结束后回调
     */
    private var endAction: Function1<SwipeNestedLayout, Unit>? = null

    /**
     * 数值改变回调拦截器,返回true就不会走默认逻辑
     */
    var onNotifyOffsetChangeHoldUp: ((realOffsetX: Int, realOffsetY: Int, isAnim: Boolean) -> Boolean)? =
        null

    /**
     * 通过动画，重置到endValue位置
     */
    fun animReset(
        endXValue: Int = resetX,
        endYValue: Int = resetY,
        duration: Long = resetAnimDuration,
        animEndAction: Function1<SwipeNestedLayout, Unit>? = null
    ) {
        if (stopAnim.isRunning) {
            return
        }
        if (realOffsetX != endXValue || realOffsetY != endYValue) {
            animEndX = endXValue
            animEndY = endYValue
            this.endAction = animEndAction
            stopAnim.duration = duration

            stopAnim.start()
        }
    }

    /**
     * 距离发生变化
     */
    open fun onNotifyOffsetChange(realOffsetX: Int, realOffsetY: Int, isAnim: Boolean) {
        if (onNotifyOffsetChangeHoldUp?.invoke(realOffsetX, realOffsetY, isAnim) != true) {
            forEach {
                it.translationY = -realOffsetY * 0.2f
                it.translationX = -realOffsetX * 0.2f
            }
        }
    }
}