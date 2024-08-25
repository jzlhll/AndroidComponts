package com.allan.nested.decoration

import android.graphics.Rect
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

/**
 * @author au
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
                                   private val rightPadding:Int) : ItemDecoration() {
    private val halfHorzPadding:Int = horzPadding shr 1
    private val halfVertPadding:Int = vertPadding shr 1

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val itemPosition = parent.getChildAdapterPosition(view)

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

        outRect.top = halfVertPadding
        outRect.bottom = halfVertPadding
    }
}