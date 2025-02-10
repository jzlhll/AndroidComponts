package com.au.module_nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * @author: au
 * @date: 2022/9/19 10:38
 * @description:
 */
class PaddingItemDecoration : ItemDecoration {
    private var padding: Int
    private var spacing: Int
    private var isVertical: Boolean

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

        val position = parent.getChildAdapterPosition(view) // item position
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