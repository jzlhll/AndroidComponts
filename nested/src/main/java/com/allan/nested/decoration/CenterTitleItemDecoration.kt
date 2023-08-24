package com.allan.nested.decoration

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.text.TextPaint
import android.view.Gravity
import androidx.recyclerview.widget.RecyclerView

class CenterTitleItemDecoration(
    var title: CharSequence?,
    stickyCall: (position: Int) -> CharSequence?,
    var stickyTitlePaddingStart:Int, //小标题文字距离左边的长度 dip(8)
    var stickyTitlePaddingEnd:Int  //小标题文字距离右边的长度  dip(8)
) : StickyItemDecoration(stickyCall, stickyTitlePaddingStart, stickyTitlePaddingEnd) {

    //小标题文字距离位置
    var stickyTitleGravity = Gravity.CENTER

    //小标题的文字颜色
    var stickyTitleColor = Color.GRAY

    //小标题文字大小
    var stickyTitleSize = 14 * 3f

    //画笔外观
    var stickyTitleTypeface = Typeface.DEFAULT

    //小标题文字画笔
    private val titlePaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stickyTitleColor
            textSize = stickyTitleSize
            textAlign = Paint.Align.CENTER
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        val left = parent.left + parent.paddingStart
        val right = parent.right - parent.paddingEnd
        //绘制文字
        title?.let {
            titlePaint.color = stickyTitleColor
            titlePaint.textSize = stickyTitleSize
            titlePaint.typeface = stickyTitleTypeface
            c.drawText(
                it,
                0, it.count(),
                getDrawTextX(right.toFloat(), left.toFloat(), it),
                decorationHeight / 2 + getDrawTextOff(),
                titlePaint
            )
        }
    }

    private fun getDrawTextX(right: Float, left: Float, text: CharSequence): Float {
        val textWidth = titlePaint.measureText(text, 0, text.count()) / 2f
        return when (stickyTitleGravity) {
            Gravity.START, Gravity.LEFT -> {
                left + stickyTitlePaddingStart + textWidth
            }
            Gravity.END, Gravity.RIGHT -> {
                right - stickyTitlePaddingEnd - textWidth
            }
            else -> {
                (right - left) / 2
            }
        }
    }

    /**
     * 获取基线和文字中间线的距离
     * 如果绘制的y为中间值，加上这个偏移量，文字效果才能是居中
     */
    private fun getDrawTextOff(): Float {
        val fontMetrics: Paint.FontMetrics = titlePaint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        return distance
    }
}