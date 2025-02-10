package com.au.module_nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * @author: au
 * @date: 2022/9/19 10:38
 * @description: 用于处理clipToPadding失效时，上下边距绘制
 */
class PaddingTopBottomItemDecoration : ItemDecoration {
    private var topPadding: Int
    private var bottomPadding: Int
    private var isVertical: Boolean

    constructor(top: Int, bottom: Int, isVertical: Boolean) {
        this.topPadding = top
        this.bottomPadding = bottom
        this.isVertical = isVertical
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        val position = parent.getChildAdapterPosition(view) // item position
        val count = parent.adapter?.itemCount ?: 0
        if (isVertical) {
            when (position) {
                0 -> {
                    outRect.top = topPadding
                }
                count - 1 -> {
                    outRect.bottom = bottomPadding
                }
            }
        }
    }
}