package com.allan.nested.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.NestedScrollingChild3
import androidx.core.view.NestedScrollingChildHelper
import androidx.core.view.ViewCompat

class NestAppBarLayout : com.google.android.material.appbar.AppBarLayout, NestedScrollingChild3 {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var mScrollingChildHelper:NestedScrollingChildHelper? = null

    private fun getScrollingChildHelper(): NestedScrollingChildHelper {
        if (mScrollingChildHelper == null) {
            mScrollingChildHelper = NestedScrollingChildHelper(this)
            mScrollingChildHelper!!.isNestedScrollingEnabled = true
        }
        return mScrollingChildHelper!!
    }

    // NestedScrollingChild
    override fun setNestedScrollingEnabled(enabled: Boolean) {
        getScrollingChildHelper().isNestedScrollingEnabled = enabled
    }

    override fun isNestedScrollingEnabled(): Boolean {
        return getScrollingChildHelper().isNestedScrollingEnabled
    }

    override fun startNestedScroll(axes: Int): Boolean {
        return getScrollingChildHelper().startNestedScroll(axes)
    }

    override fun startNestedScroll(axes: Int, type: Int): Boolean {
        return getScrollingChildHelper().startNestedScroll(axes, type)
    }

    override fun stopNestedScroll() {
        getScrollingChildHelper().stopNestedScroll()
    }

    override fun stopNestedScroll(type: Int) {
        getScrollingChildHelper().stopNestedScroll(type)
    }

    override fun hasNestedScrollingParent(): Boolean {
        return getScrollingChildHelper().hasNestedScrollingParent()
    }

    override fun hasNestedScrollingParent(type: Int): Boolean {
        return getScrollingChildHelper().hasNestedScrollingParent(type)
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?
    ): Boolean {
        return getScrollingChildHelper().dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int
    ): Boolean {
        return getScrollingChildHelper().dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow, type
        )
    }

    override fun dispatchNestedScroll(
        dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int,
        dyUnconsumed: Int, offsetInWindow: IntArray?, type: Int, consumed: IntArray
    ) {
        getScrollingChildHelper().dispatchNestedScroll(
            dxConsumed, dyConsumed,
            dxUnconsumed, dyUnconsumed, offsetInWindow, type, consumed
        )
    }

    override fun dispatchNestedPreScroll(dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?): Boolean {
        return getScrollingChildHelper().dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow)
    }

    override fun dispatchNestedPreScroll(
        dx: Int, dy: Int, consumed: IntArray?, offsetInWindow: IntArray?,
        type: Int
    ): Boolean {
        return getScrollingChildHelper().dispatchNestedPreScroll(
            dx, dy, consumed, offsetInWindow,
            type
        )
    }

    override fun dispatchNestedFling(velocityX: Float, velocityY: Float, consumed: Boolean): Boolean {
        return getScrollingChildHelper().dispatchNestedFling(velocityX, velocityY, consumed)
    }

    override fun dispatchNestedPreFling(velocityX: Float, velocityY: Float): Boolean {
        return getScrollingChildHelper().dispatchNestedPreFling(velocityX, velocityY)
    }
}