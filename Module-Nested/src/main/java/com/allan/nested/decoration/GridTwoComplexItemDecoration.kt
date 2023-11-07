package com.allan.nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * @author allan
 *
 * 两竖item往下的Grid排列方式
 * top表示两竖模式下的，两行之间的高度间隔；itemLeft是2个之间的距离。
 * 2023.04.24 追加，left和right，用于写左右Item的padding
 *
 * 使用了这个以后，则不再需要设置RecyclerView的padding了。
 */
class GridTwoComplexItemDecoration(vertPadding: Int,
                                   horzPadding: Int,
                                   private val leftPadding:Int,
                                   private val rightPadding:Int,
                                   private val firstTop:Int,
                                   private val lastBottom:Int) : ItemDecoration() {
    private val halfHorzPadding:Int
    private val halfVertPadding:Int
    init {
        this.halfHorzPadding = horzPadding shr 1
        this.halfVertPadding = vertPadding shr 1
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemPosition = parent.getChildAdapterPosition(view)
        val count = parent.adapter?.itemCount ?: 0

        val isTopOne = itemPosition <= 1
        val isBottomOne = if(count % 2 == 0) itemPosition >= count - 2 else itemPosition >= count - 1

        //进行左右分半，实现这个边距。否则，就会造成一边宽一边窄
        if (itemPosition % 2 == 1) {
            //右item
            outRect.left = halfHorzPadding
            outRect.right = rightPadding
        } else {
            //左item
            outRect.right = halfHorzPadding
            outRect.left = leftPadding
        }

        outRect.top = if(isTopOne) firstTop else halfVertPadding
        outRect.bottom = if(isBottomOne) lastBottom else halfVertPadding
    }
}