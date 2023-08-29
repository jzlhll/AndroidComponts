package com.allan.nested.decoration

import android.graphics.Canvas
import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * recyclerView的装饰器
 */
abstract class BaseItemDecoration<T>(val itemBlock: (Int) -> T?) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        //获取在列表中的位置
        val position = parent.getChildAdapterPosition(view)
        itemBlock.invoke(position)?.let {
            getItemOffsets(position, it, outRect, view)
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        //获取当前屏幕显示的item数量
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val childView: View = parent.getChildAt(i)
            //获取在列表中的位置
            val position = parent.getChildAdapterPosition(childView)
            itemBlock.invoke(position)?.let {
                onDrawItem(c, parent, position, it, childView)
            }
        }
    }

    abstract fun getItemOffsets(position: Int, data: T, outRect: Rect, itemView: View)

    abstract fun onDrawItem(
        canvas: Canvas,
        parent: RecyclerView,
        position: Int,
        data: T,
        itemView: View
    )
}