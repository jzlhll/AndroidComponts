package com.au.module_nested.decoration

import android.graphics.*
import android.text.TextPaint
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import androidx.core.view.marginBottom
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max

/**
 * 描述：支持粘性头部的装饰器,只支持[decorationHeight]小于itemView高度的情况
 */
open class StickyItemDecoration(
    private val stickyCall: (position: Int) -> CharSequence?/*获取小标题，已经根据小标题分类来绘制*/,
    //小标题文字距离左边的长度  = dip(8)
    var stickyTextPaddingStart:Int,
    //小标题文字距离右边的长度  = dip(8)
    var stickyTextPaddingEnd : Int) : RecyclerView.ItemDecoration() {
    //------------------------------可设置参数 start----------------------------//
    //小标题的文字颜色
    var stickyTextColor = Color.GRAY

    //小标题文字大小
    var stickyTextSize = 14 * 3f

    //小标题背景
    var stickyBackgroundColor: Int? = null

    //背景间距
    var stickyBgPaddingStart = 0

    //背景间距
    var stickyBgPaddingEnd = 0

    //小标题文字距离位置
    var stickyTextGravity = Gravity.END

    //小标题高度
    var decorationHeight = 32 * 3

    //画笔外观
    var stickyTextTypeface = Typeface.DEFAULT

    //----------------------------------------------------------//
    //小标题背景画笔
    val decorationPaint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG)
    }

    //小标题文字画笔
    val textPaint by lazy {
        TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stickyTextColor
            textSize = stickyTextSize
            textAlign = Paint.Align.CENTER
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        //获取在列表中的位置
        val position = parent.getChildAdapterPosition(view)
        //如果返回true才绘制
        if (position == 0 || stickyCall.invoke(position - 1) !=
            stickyCall.invoke(position)
        ) {
            outRect.top = decorationHeight
        } else {
            outRect.top = 0
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)
        //获取所有item的数量
        val itemCount = state.itemCount
        //获取当前屏幕显示的item数量
        val childCount = parent.childCount
        val left = parent.left + parent.paddingStart
        val right = parent.right - parent.paddingEnd
        var preTag: CharSequence?
        var curTag: CharSequence? = null
        for (i in 0 until childCount) {
            val childView: View = parent.getChildAt(i)
            //获取在列表中的位置
            val position = parent.getChildAdapterPosition(childView)
            preTag = curTag
            curTag = stickyCall.invoke(position)
            //如果两个item属于同一个tag，就不绘制
            if (curTag == null || TextUtils.equals(preTag, curTag)) {
                continue
            }
            //获取item 的bottom
            val bottom = childView.bottom + childView.marginBottom
            //计算出tag的bottom,取最大值，这样就会悬浮
            var tagBottom = max(decorationHeight, childView.top).toFloat()
            //判断是否是最后一个
            if (position + 1 < itemCount) {
                //获取下个tag,需要当前position+1
                val nextTag: CharSequence? = stickyCall.invoke(position + 1)
                //被顶起来的条件 当前tag与下个tag不等且item的bottom已小于分割线高度，否则就绘制之前的
                // 然后第一个item的bottom设置为分割线的bottom
                if (!TextUtils.equals(curTag, nextTag) && bottom < tagBottom) {
                    tagBottom = bottom.toFloat()
                }
            }
            //绘制tag背景
            stickyBackgroundColor?.let {
                decorationPaint.color = it
                onDrawBackground(
                    c,
                    left.toFloat() + stickyBgPaddingStart,
                    tagBottom - decorationHeight,
                    right.toFloat() - stickyBgPaddingEnd,
                    tagBottom,
                    decorationPaint
                )
            }
            //绘制文字
            textPaint.color = stickyTextColor
            textPaint.textSize = stickyTextSize
            textPaint.typeface = stickyTextTypeface
            onDrawText(
                c,
                curTag,
                getDrawTextX(right.toFloat(), left.toFloat(), curTag),
                tagBottom - decorationHeight / 2 + getDrawTextOff(),
                textPaint
            )
        }
    }

    private fun getDrawTextX(right: Float, left: Float, text: CharSequence): Float {
        val textWidth = textPaint.measureText(text, 0, text.count()) / 2f
        return when (stickyTextGravity) {
            Gravity.START, Gravity.LEFT -> {
                left + stickyTextPaddingStart + textWidth
            }
            Gravity.END, Gravity.RIGHT -> {
                right - stickyTextPaddingEnd - textWidth
            }
            else -> {
                (right - left) / 2
            }
        }
    }

    /**
     * 绘制背景
     */
    open fun onDrawBackground(
        c: Canvas,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        paint: Paint
    ) {
        c.drawRect(left, top, right, bottom, paint)
    }

    /**
     * 绘制背景
     */
    open fun onDrawText(
        c: Canvas,
        curTag: CharSequence,
        x: Float,
        y: Float,
        paint: TextPaint
    ) {
        c.drawText(curTag, 0, curTag.count(), x, y, paint)
    }


    /**
     * 获取基线和文字中间线的距离
     * 如果绘制的y为中间值，加上这个偏移量，文字效果才能是居中
     */
    private fun getDrawTextOff(): Float {
        val fontMetrics: Paint.FontMetrics = textPaint.fontMetrics
        val distance = (fontMetrics.bottom - fontMetrics.top) / 2 - fontMetrics.bottom
        return distance
    }
}