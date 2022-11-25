package com.allan.androidlearning.views

import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart

/**
 * author: allan.jiang
 * Time: 2022/11/25
 * Desc: 用于嵌套title，tabLayout的头部View+recyclerView的嵌套场景。
 * 并且用于下拉刷新的逻辑。
 */
open class NestedCoordinatorLayout : CoordinatorLayout{
    constructor(context: Context, attrs: AttributeSet?) :super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :super(context, attrs, defStyleAttr)

    private val PULL_DOWN_NONE = -1
    private val PULL_DOWN_NOT_ACCEPT = 0
    private val PULL_DOWN_ACCEPTED = 1

    //当进入我们需要的拉动场景的标记
    private var isOncePullDownScroll = PULL_DOWN_ACCEPTED
    fun isPullDownState() = isOncePullDownScroll == PULL_DOWN_ACCEPTED

    //当我回弹的时候，拦截掉触摸事件
    var abortTouch = false

    private var realOffsetY = 0f

    enum class PullDownShrinkState {
        START,
        MOVING,
        END
    }

    var pullDownAnimDuration = 330L

    /**
     * 当满足了，子target不能移动，全部传导到本layout时，往下拉刷新的情况，
     * 随着拉动不断的回调offsetY。
     *
     * 具体查看 isPullDownState()，isOncePullDownScroll， onNestedPreScroll等逻辑。
     */
    var pullDownScrollingCallback:((offY:Float)->Unit)? = null

    /**
     * 当拉动动作完成，回弹的时候，进行三类动作：开始，停止，回弹中。
     */
    var pullDownShrinkCallback:((offY:Float, state:PullDownShrinkState)->Unit)? = null

    /**
     * 当处于loadingData状态的时候，通过forceResetAnim调度，再次回弹通过这个回调。
     */
    var pullDownForceShrinkCallback:((ratio:Float, state:PullDownShrinkState)->Unit)? = null

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (abortTouch) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    private var animOffY = 0f
    private val stopAnim by lazy(LazyThreadSafetyMode.NONE) {
        ValueAnimator
            .ofInt(0, 0)
            .apply {
                duration = pullDownAnimDuration
            doOnStart {
                abortTouch = true
                animOffY = realOffsetY
                pullDownShrinkCallback?.invoke(-realOffsetY, PullDownShrinkState.START)
            }
            addUpdateListener {
                val f = 1 - it.animatedFraction
                realOffsetY = (animOffY * f)
                pullDownShrinkCallback?.invoke(-realOffsetY, PullDownShrinkState.MOVING)
            }
            doOnEnd {
                abortTouch = false
                pullDownShrinkCallback?.invoke(-realOffsetY, PullDownShrinkState.END)
            }
        }
    }
    private val forceResetAnim by lazy(LazyThreadSafetyMode.NONE) {
        ValueAnimator
        .ofInt(0, 0)
        .apply {
            duration = pullDownAnimDuration * 2 / 3 //较快的直接回去
            doOnStart {
                abortTouch = true
                animOffY = realOffsetY
                pullDownForceShrinkCallback?.invoke(-realOffsetY, PullDownShrinkState.START)
            }
            addUpdateListener {
                val f = 1 - it.animatedFraction
                realOffsetY = (animOffY * f)
                pullDownForceShrinkCallback?.invoke(-realOffsetY, PullDownShrinkState.MOVING)
            }
            doOnEnd {
                abortTouch = false
                pullDownForceShrinkCallback?.invoke(0f, PullDownShrinkState.END)
            }
        }
    }

    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        if (isPullDownState()) {
            consumed[0] = dx
            consumed[1] = dy //这里我们进行全部消费，later: 虽然我认为没必要，子View也滑不动，因为我们只在顶部的时候往下拉的场景。
            realOffsetY += dy //因为子View传导是一段一段的传递的，所以需要统计出总共滑动的距离
            onPullDownTrigger(target, realOffsetY, consumed)
        }

        super.onNestedPreScroll(target, dx, dy, consumed, type)
    }

    override fun onNestedScroll(
        target: View,
        dxConsumed: Int,
        dyConsumed: Int,
        dxUnconsumed: Int,
        dyUnconsumed: Int,
        type: Int,
        consumed: IntArray
    ) {
        //只有当子View不做刷新(dyConsumed == 0)；而且方向是往下滑(即dyUnconsumed<0) 我们认为进入了下拉场景。
        //但是，还得考虑必须是从没有动作的场景进入的。即之前是stopNested的。
        if (isOncePullDownScroll == PULL_DOWN_NONE) {
            isOncePullDownScroll = if (dyConsumed == 0 && dyUnconsumed < 0) {
                PULL_DOWN_ACCEPTED
            } else {
                PULL_DOWN_NOT_ACCEPT
            }
        }

        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        onPullDownReleased()
        super.onStopNestedScroll(target, type)
    }

    /**
     * 当我们拉动下滑，还没有松手的时候，一直回调这个。
     * 可以考虑放开，继承干点别的事情。
     */
    private fun onPullDownTrigger(target: View, realOffsetY: Float, consumed: IntArray) {
        if(stopAnim.isRunning) stopAnim.cancel()
        val offY = -realOffsetY
        pullDownScrollingCallback?.invoke(offY)
    }

    fun onPullDownReleased() {
        isOncePullDownScroll = PULL_DOWN_NONE
        if (realOffsetY != 0f) {
            stopAnim.start()
        }
    }

    fun forceResetAnim() {
        forceResetAnim.start()
    }
}