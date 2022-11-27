package com.allan.androidlearning.rcv.itemdecoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

class PaddingItemDecoration : RecyclerView.ItemDecoration {
    private var padding: Int
    private var spacing: Int
    private var isVertical: Boolean

    /**
     * recyclerView的间隔。padding是用于最上面和最下面的位置；spacing是用于item之间的间距。
     */
    constructor(padding: Int, spacing: Int, isVertical: Boolean) {
        this.padding = padding
        this.spacing = spacing
        this.isVertical = isVertical
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        val position = parent.getChildAdapterPosition(view)
        val count = parent.adapter?.itemCount ?: 0
        if (isVertical) {
            when (position) {
                0 -> {
                    outRect.top = padding
                }
                count - 1 -> {
                    outRect.top = spacing
                    outRect.bottom = padding
                }
                else -> {
                    outRect.top = spacing
                }
            }
        } else {
            when (position) {
                0 -> {
                    outRect.left = padding
                }
                count - 1 -> {
                    outRect.left = spacing
                    outRect.right = padding
                }
                else -> {
                    outRect.left = spacing
                }
            }
        }
    }
}