package com.allan.nested.layout

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat

/**
 * author: allan
 * Time: 2022/11/25
 * Desc: 组合 CircularProgressIndicator, RecyclerView + AppBarLayout 嵌套场景。
 * 实现用于下拉刷新，组合头前滑动和变幻的逻辑。
 * 实现的是单嵌套。
 */
open class NestedCoordinatorLayout : CoordinatorLayout {
    constructor(context: Context, attrs: AttributeSet?) :super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :super(context, attrs, defStyleAttr)

    val refresher by lazy(LazyThreadSafetyMode.NONE) { NestedLayoutRefresher(this) }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (NestedLayoutRefresher.DEBUG) Log.d(NestedLayoutRefresher.TAG, "${refresher.abortTouch} onInterceptTouchEvent ${ev?.y} ${ev?.action}")

        if (refresher.abortTouch) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }

    ///////{1.
    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int, consumed: IntArray) {
        refresher.onHostNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed)
    }
//    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type)
//    }
//    override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int) {
//        super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed)
//    }
    /////// }

    ///// {2.
    override fun onStopNestedScroll(target: View) {
        refresher.onHostPullDownReleased()
        super.onStopNestedScroll(target, ViewCompat.TYPE_TOUCH) //必须这样，避免父类二次调用到下面那个函数。
    }
    override fun onStopNestedScroll(target: View, type: Int) {
        refresher.onHostPullDownReleased()
        super.onStopNestedScroll(target, type)
    }
    ////// }

    ////// {3.
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray) {
        refresher.onHostNestedPreScroll(target, dx, dy, consumed)
        super.onNestedPreScroll(target, dx, dy, consumed, ViewCompat.TYPE_TOUCH) //必须这样，避免父类二次调用到下面那个函数。
    }
    override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
        refresher.onHostNestedPreScroll(target, dx, dy, consumed)
        super.onNestedPreScroll(target, dx, dy, consumed, type)
    }
    ////}
}