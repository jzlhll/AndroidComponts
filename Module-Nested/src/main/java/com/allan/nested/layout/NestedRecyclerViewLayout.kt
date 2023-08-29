package com.allan.nested.layout

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.recyclerview.widget.RecyclerView
import com.au.module_android.utils.dpGlobal
import com.google.android.material.progressindicator.CircularProgressIndicator

/**
 * @author allan.jiang
 * Date: 2023/2/17
 * Description 使用NestedCoordinatorLayout包裹住RecyclerView，能下拉动作的转圈圈进度指示器。
 *
 * 这样，这个控件即可用做常规的显示带刷新的list控件。还可以节约一层控件。
 *
 * 如果你需要真实刷新，请设定 [nestedPullManager].onRefreshAction。
 */
class NestedRecyclerViewLayout : NestedConstraintLayout {
    constructor(context: Context) :super(context, null)
    constructor(context: Context, attrs: AttributeSet?) :super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) :super(context, attrs, defStyleAttr)

    val progressIndicator = CircularProgressIndicator(context)
    val recyclerView = RecyclerView(context)

    init {
        addProgressIndicator()
        addRecyclerView()

        refresher.initEarlyAsSmooth(recyclerView, progressIndicator, false)
    }

    private fun addProgressIndicator() {
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        lp.bottomToTop = PARENT_ID
        lp.startToStart = PARENT_ID
        lp.endToEnd = PARENT_ID
        lp.bottomMargin = 16f.dpGlobal.toInt() //这样，就让indicator比top还要高16dp。就会比rcv慢再出现达成目的

        progressIndicator.indicatorSize = 20f.dpGlobal.toInt()
        progressIndicator.trackThickness = 2f.dpGlobal.toInt()
        progressIndicator.visibility = View.GONE
        addView(progressIndicator, lp)
    }

    private fun addRecyclerView() {
        val lp = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
        recyclerView.overScrollMode = OVER_SCROLL_NEVER
        recyclerView.clipToPadding = false
        recyclerView.setPadding(0, 0, 0, 30f.dpGlobal.toInt())

        addView(recyclerView, 0, lp) //添加到了最底层
    }
}