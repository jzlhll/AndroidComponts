package com.au.module_nested.help

import android.graphics.Canvas
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

/**
 * @author allan
 * @date :2024/10/18 16:44
val itemTouchHelper = ItemTouchHelper(SwipeToDeleteCallback().also {
it.setMaxSwipeToLeftX(70.dp)
})
itemTouchHelper.attachToRecyclerView(rcv)

 在bindData(bean)
 记得
itemView.scrollTo(0, 0) //还原复用的结果。
 */
class SwipeToDeleteCallback : ItemTouchHelper.Callback() {
    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val dragFlags = 0
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(dragFlags, swipeFlags)
    }

    //拖拽切换Item的recyclerView框架回调
    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    //拖动换位置item的recyclerView框架回调
    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

    //item被选中的框架回调
//    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
//        super.onSelectedChanged(viewHolder, actionState)
//        logd("onSelecteddChanged $actionState")
//    }

    override fun getSwipeThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return 2f //条件1：滑动的触发滑动比例。超过1就不可能触发。
    }

    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Float.MAX_VALUE //条件2：侧滑距离速率。
    }

    private var mMaxSwipeToLeftX = 0f
    private var mTriggerToLeftX = 0f

    fun setMaxSwipeToLeftX(x:Float) {
        mMaxSwipeToLeftX = x
        mTriggerToLeftX = x * 0.8f
    }

    private var mCurrentScrollX = 0
    private var mCurrentScrollXWhenInactive = 0
    private var mInitXWhenInactive = 0f
    private var mFirstInactive = false

    private val sNoRight = true //不允许向右

    private fun limitX(x:Float): Int {
        return (min(x, mMaxSwipeToLeftX) + 0.5f).toInt()
    }

    override fun onChildDraw(
        c: Canvas,
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        dX: Float,
        dY: Float,
        actionState: Int,
        isCurrentlyActive: Boolean
    ) {
        // 首次滑动时，记录下ItemView当前滑动的距离
        if (dX == 0f) {
            mCurrentScrollX = viewHolder.itemView.scrollX
            mFirstInactive = true
        }
        if (isCurrentlyActive) {// 手指滑动
            // 基于当前的距离滑动
            var delta = mCurrentScrollX - dX
            if (sNoRight) {
                if (delta < 0) {
                    delta = 0f
                }
            }
            viewHolder.itemView.scrollTo(limitX(delta), 0)
        } else { // 动画滑动
            if (mFirstInactive) {
                mFirstInactive = false
                mCurrentScrollXWhenInactive = viewHolder.itemView.scrollX
                mInitXWhenInactive = dX
            }
            if (viewHolder.itemView.scrollX >= mTriggerToLeftX) {
                // 当手指松开时，ItemView的滑动距离大于给定阈值，那么最终就停留在阈值，显示删除按钮。
                val x = max(mCurrentScrollX - dX, mMaxSwipeToLeftX)
                viewHolder.itemView.scrollTo(limitX(x), 0)
            } else {
                // 这里只能做距离的比例缩放，因为回到最初位置必须得从当前位置开始，dx不一定与ItemView的滑动距离相等
                val x = mCurrentScrollXWhenInactive * dX / mInitXWhenInactive
                viewHolder.itemView.scrollTo(limitX(x), 0)
            }
        }
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        if (viewHolder.itemView.scrollX >= mTriggerToLeftX) {
            viewHolder.itemView.scrollTo(limitX(mMaxSwipeToLeftX), 0)
        } else if (viewHolder.itemView.scrollX < 0) {
            viewHolder.itemView.scrollTo(0, 0)
        }
    }

    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    override fun isItemViewSwipeEnabled(): Boolean {
        return true
    }
}