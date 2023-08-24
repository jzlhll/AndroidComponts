package com.allan.nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * @author: Allan.jiang
 * @date: 2023.04.24
 * @description: 竖条的单个Item的上下间距即可。
 *
 */
class VertPaddingItemDecoration(
    private val firstTopPadding: Int,
    private val endBottomPadding: Int,
    padding: Int,
) : ItemDecoration() {

    private val halfPadding:Int = padding shr 1

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {

        val position = parent.getChildAdapterPosition(view) // item position
        val count = parent.adapter?.itemCount ?: 0
        when (position) {
            0 -> {
                outRect.top = firstTopPadding
                outRect.bottom = halfPadding
            }
            count - 1 -> {
                outRect.top = halfPadding
                outRect.bottom = endBottomPadding
            }
            else -> {
                outRect.top = halfPadding
                outRect.bottom = halfPadding
            }
        }
    }
}