package com.allan.nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * @author allan.jiang
 * 4个一行的GridItemDecoration
 * @param top: 表示间距上的距离。
 * @param horzPadding: 表示每个间隔的左右间距。
 * @param horzCount: 横向个数
 *
 * 内部实现会根据最左边还是最右边，还是中间，来决定left，right的宽度。
 */
class GridMultiDownItemDecoration(private val top: Int, horzPadding: Int, private val horzCount:Int) : RecyclerView.ItemDecoration() {
    private val halfHorzPadding:Int
    private val horzCount_1:Int //横向个数减1
    init {
        this.halfHorzPadding = horzPadding / 2
        horzCount_1 = horzCount
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemPosition = parent.getChildAdapterPosition(view)
        //第一个只设置右padding；最后一个设置左padding。其他2个设置。
        if (itemPosition % horzCount == 0) {
            outRect.right = halfHorzPadding
        } else if (itemPosition % horzCount == horzCount_1) {
            outRect.left = halfHorzPadding
        } else {
            outRect.left = halfHorzPadding
            outRect.right = halfHorzPadding
        }
        outRect.top = top
    }
}