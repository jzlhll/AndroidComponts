package com.allan.androidlearning.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.progressindicator.CircularProgressIndicator

class BindListView : SwipeExNestedLayout {
    val progressIndicator by lazy(LazyThreadSafetyMode.NONE) { CircularProgressIndicator(context) }

    val recyclerView by lazy(LazyThreadSafetyMode.NONE)  { RecyclerView(context) }

    /**
     * 是否允许响应用户操作
     */
    var isUserInputEnabled = true
    /**
     * 是否允许下拉刷新
     */
    var enablePullRefresh = true

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
    }

    init {
        isNestedScrollingEnabled = true
        resetAnimDuration = 320
        addRingView()
        addRecyclerView()
    }

    /**
     * 是否支持嵌套滚动
     */
    var enableNestedScrollingEnabled = true

    /**
     * 布局管理器
     */
    var layoutManager
        get() = recyclerView.layoutManager
        set(value) {
            recyclerView.layoutManager = value
        }

    /**
     * 添加下拉指示器
     */
    private fun addRingView() {
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.bottomToTop = LayoutParams.PARENT_ID
        lp.startToStart = LayoutParams.PARENT_ID
        lp.endToEnd = LayoutParams.PARENT_ID
        lp.bottomMargin = 16
        progressIndicator.indicatorSize = 20
        progressIndicator.trackThickness = 2
        addView(progressIndicator, lp)
    }

    private fun addRecyclerView() {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        lp.topToTop = LayoutParams.PARENT_ID
        lp.startToStart = LayoutParams.PARENT_ID
        lp.endToEnd = LayoutParams.PARENT_ID
        lp.bottomToBottom = LayoutParams.PARENT_ID
        recyclerView.overScrollMode = OVER_SCROLL_NEVER
        addView(recyclerView, lp)
    }

    /**
     * 是否允许刷新
     */
    private var _enableRefresh: Boolean = true
        get() = field && enablePullRefresh

    override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
        if (!enableNestedScrollingEnabled) {
            return false
        }
        return super.onStartNestedScroll(child, target, axes, type)
    }

    /**
     * 滚动到第一个
     */
    fun scrollToFirst() {
        when (val manager = layoutManager) {
            is LinearLayoutManager -> {
                manager.scrollToPositionWithOffset(0, 0)
            }
            is GridLayoutManager -> {
                manager.scrollToPositionWithOffset(0, 0)
            }
            is StaggeredGridLayoutManager -> {
                manager.scrollToPositionWithOffset(0, 0)
            }
            is RecyclerView.LayoutManager -> {
                manager.scrollToPosition(0)
            }
            else -> {
                recyclerView.scrollToPosition(0)
            }
        }
    }

    /**
     * 停止刷新
     */
    fun stopRefresh() {
        _enableRefresh = true
        isIndeterminate(false)
        isNestedScrollingEnabled = true
        animReset()
    }

    /**
     * 触发下拉刷新的值
     */
    var pullRefreshValue = 80f

    /**
     * 是否正在刷新中
     */
    val isRefreshing
        get() = false

    override fun onNotifyOffsetChange(realOffsetX: Int, realOffsetY: Int, isAnim: Boolean) {
        val isHoldUp = onNotifyOffsetChangeHoldUp?.invoke(realOffsetX, realOffsetY, isAnim) == true
        //是否拦截
        if (isHoldUp) {
            return
        }
        val moveY = -realOffsetY * offsetRatio
        if (isRefreshing) {
            return
        }
        if (!_enableRefresh) {
            //正在刷新中或者不允许下拉刷新
            recyclerView.translationY = moveY
            return
        }
        progressIndicator.translationY = moveY
        recyclerView.translationY = moveY
        isIndeterminate(false)
        val progress =
            (-realOffsetY * offsetRatio * progressIndicator.max / pullRefreshValue).toInt()
        progressIndicator.progress = progress
    }

    private fun isIndeterminate(b: Boolean) {
        if (progressIndicator.isIndeterminate == b) {
            return
        }
        progressIndicator.isIndeterminate = b
    }

    override fun onStopNestedScroll(target: View, type: Int) {
        if (type == ViewCompat.TYPE_TOUCH) {
            if (false) { //todo 暂时改成直接结束 isRefreshing
                return
            }
            val refreshStopY = -(pullRefreshValue / offsetRatio).toInt()
            if (progressIndicator.progress >= progressIndicator.max) {
                isNestedScrollingEnabled = false
                animReset(endYValue = refreshStopY) {
                    _enableRefresh = false
                    isIndeterminate(true)
                }
            } else {
                animReset()
            }
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        if (!isUserInputEnabled) {
            return true
        }
        return super.onInterceptTouchEvent(ev)
    }
}