package com.au.module_nested.layout

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.recyclerview.widget.RecyclerView
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.dp
import com.au.module_nested.R
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * @author au
 * Date: 2023/2/17
 * Description 使用NestedCoordinatorLayout包裹住RecyclerView，能下拉动作的转圈圈进度指示器。
 *
 * 这样，这个控件即可用做常规的显示带刷新的list控件。还可以节约一层控件。
 *
 * 如果你需要真实刷新，请设定 [nestedPullManager].onRefreshAction。
 *
 * hasIndicatorInLayout 或者 hasIndicatorInDecorView 优先生效hasIndicatorInLayout。
 */
class NestedRecyclerViewLayout : NestedConstraintLayout {
    constructor(context: Context) :super(context, null) {
        initial(false, false, 0f)
    }

    constructor(context: Context, hasIndicatorInLayout: Boolean, hasIndicatorInDecorView : Boolean, holdDeltaY: Float) :super(context, null) {
        initial(hasIndicatorInLayout, hasIndicatorInDecorView, holdDeltaY)
    }

    constructor(context: Context, attrs: AttributeSet?) :super(context, attrs) {
        val triple = readIndicatorAttrs(attrs, context)
        initial(triple.first, triple.second, triple.third)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :super(context, attrs, defStyleAttr) {
        val triple = readIndicatorAttrs(attrs, context)
        initial(triple.first, triple.second, triple.third)
    }

    private fun readIndicatorAttrs(attrs: AttributeSet?, context: Context): Triple<Boolean, Boolean, Float> {
        if (attrs != null) {
            val sa = context.obtainStyledAttributes(attrs, R.styleable.NestedRecyclerViewLayout)
            val hasIndicatorInLayout = sa.getBoolean(R.styleable.NestedRecyclerViewLayout_hasIndicatorInLayout, false)
            val hasIndicatorInDecorView = sa.getBoolean(R.styleable.NestedRecyclerViewLayout_hasIndicatorInDecorView, false)
            val holdDeltaY = sa.getDimension(R.styleable.NestedRecyclerViewLayout_holdDeltaY, 0f)
            sa.recycle()
            return Triple(hasIndicatorInLayout, hasIndicatorInDecorView, holdDeltaY)
        }
        return Triple(false, false, 0f)
    }

    val recyclerView = RecyclerView(context)

    /**
     * 创建一个进度圈
     */
    private fun createCircularProgressIndicator(context: Context): CircularProgressIndicator {
        val progressIndicator = CircularProgressIndicator(context)
        progressIndicator.indicatorSize = 20.dp
        progressIndicator.trackThickness = 2.dp
        progressIndicator.visibility = View.GONE
        return progressIndicator
    }

    private fun initial(hasIndicatorInLayout:Boolean, hasIndicatorInDecorView: Boolean, holdDeltaY:Float) {
        if (!isInEditMode) {
            addRecyclerView()

            val indicator = if (hasIndicatorInLayout) {
                createCircularProgressIndicator(context).also {
                    val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                    lp.bottomToTop = PARENT_ID
                    lp.startToStart = PARENT_ID
                    lp.endToEnd = PARENT_ID
                    lp.bottomMargin = 16.dp //这样，就让indicator比top还要高16dp。就会比rcv慢再出现达成目的
                    addView(it, lp)
                }
            } else if (hasIndicatorInDecorView) {
                val decorView = context.asOrNull<Activity>()?.window?.decorView.asOrNull<FrameLayout>()
                if (decorView != null) {
                    createCircularProgressIndicator(context).also {
                        val lp = FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
                        lp.bottomMargin = 16.dp //这样，就让indicator比top还要高16dp。就会比rcv慢再出现达成目的
                        lp.gravity = Gravity.CENTER or Gravity.TOP
                        decorView.addView(it, lp)
                    }
                } else {
                    null
                }
            } else {
                null
            }

            refresher.initEarlyAsSmooth(recyclerView, indicator, false)
            refresher.setIndicatorDeltaHoldY(holdDeltaY)
        }
    }

    private fun addRecyclerView() {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        recyclerView.overScrollMode = OVER_SCROLL_NEVER
        recyclerView.clipToPadding = false
        recyclerView.setPadding(0, 0, 0, 30.dp)

        addView(recyclerView, 0, lp) //添加到了最底层
    }

    /**
     * 添加额外的indicator的位移Hold偏差值
     */
    fun setIndicatorDeltaHoldY(delta:Float) {
        refresher.setIndicatorDeltaHoldY(delta)
    }
}