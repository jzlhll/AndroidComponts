package com.au.module_nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * @author: Allan
 * @date: 2023.04.24
 * @description: 竖条的单个Item的上下间距即可。
 *
 */
@Deprecated("需要小心使用；前2个参数设置以后，不得adpater.appendData 或者 删除，可能就会导致padding问题，只用每次都sumbitList的场景。")
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