package com.allan.nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author au
 * 4个一行的GridItemDecoration
 * @param top: 表示间距上的距离。
 * @param horzPadding: 表示每个间隔的左右间距。
 * @param horzCount: 横向个数
 *
 * 内部实现会根据最左边还是最右边，还是中间，来决定left，right的宽度。
 */
class GridMultiDownItemDecoration(val top: Int, val horzPadding: Int, val horzCount:Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)
        val column = position % horzCount // view 所在的列

        outRect.left = column * horzPadding / horzCount // column * (列间距 * (1f / 列数))
        outRect.right = horzPadding - (column + 1) * horzPadding / horzCount // 列间距 - (column + 1) * (列间距 * (1f /列数))
        outRect.top = top
    }
}