package com.au.module_nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * @author: Allan
 * @date: 2023.04.24
 * @description: 使用RecyclerView横向间距。比隔壁的类优势在于，有的时候，我们只需要处理最左最右的padding。
 *
 * 使用这个，则已经处理了最左右的2个边距；则无需处理padding。
 */
@Deprecated("需要小心使用；前2个参数设置以后，不得adpater.appendData 或者 删除，可能就会导致padding问题, 只用每次都sumbitList的场景。")
class HorzPaddingItemDecoration(
    private val firstLeftPadding: Int,
    private val endRightPadding: Int,
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
                outRect.left = firstLeftPadding
                outRect.right = halfPadding
            }
            count - 1 -> {
                outRect.left = halfPadding
                outRect.right = endRightPadding
            }
            else -> {
                outRect.left = halfPadding
                outRect.right = halfPadding
            }
        }
    }
}