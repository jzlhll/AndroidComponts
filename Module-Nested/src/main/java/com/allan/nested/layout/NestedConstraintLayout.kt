package com.allan.nested.layout

import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.NestedScrollingParent3
import androidx.core.view.NestedScrollingParentHelper

/**
 * author: allan
 * Time: 2022/11/25
 * Desc: 组合 CircularProgressIndicator, RecyclerView + AppBarLayout 嵌套场景。
 * 实现用于下拉刷新，组合头前滑动和变幻的逻辑。
 * 实现的是单嵌套。
 */
open class NestedConstraintLayout : ConstraintLayout, NestedScrollingParent3{
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) :super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :super(context, attrs, defStyleAttr)

    val refresher by lazy(LazyThreadSafetyMode.NONE) { NestedLayoutRefresher(this) }

    private val mParentHelper by lazy {
        NestedScrollingParentHelper(this)
    }

    init {
        isNestedScrollingEnabled = true
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (refresher.abortTouch) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    ///////{1. 这一组全部透到底部
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }

    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
        onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
        refresher.onHostNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
    }
    /////// }

    ///// {2.
    override fun onStopNestedScroll(target: View, type: Int) {
        mParentHelper.onStopNestedScroll(target, type)
        onStopNestedScroll(target)
    }
    override fun onStopNestedScroll(target: View) {
        refresher.onHostPullDownReleased()
        super.onStopNestedScroll(target)
    }
    ////// }

    ////// {3.
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        onNestedPreScroll(target, dx, dy, consumed)
    }
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        refresher.onHostNestedPreScroll(target, dx, dy, consumed)
        super.onNestedPreScroll(target, dx, dy, consumed)
    }
    //// }

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        return isEnabled && isNestedScrollingEnabled
    }

    override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {
        mParentHelper.onNestedScrollAccepted(child, target, axes, type)
    }

    override fun getNestedScrollAxes(): Int {
        return mParentHelper.nestedScrollAxes
    }
}