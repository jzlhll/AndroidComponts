package com.au.module_android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import kotlin.math.max
import kotlin.math.min

/**
 * @author allan
 * @date :2024/5/20 17:32
 * @description: 00:20 / 03:00的格式。为了让每个字符相同宽度
 */
open class MonospaceTimeView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    fun convertDpToPixel(dp: Float, context: Context): Float {
        val metrics = context.resources.displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px
    }

    open val mColor = Color.BLACK

    //字体大小，其实就是整个View的高度
    open val mTextSize = convertDpToPixel(16f, context) //这是一个随意的值，必须通过initParamOnCreate设置。

    protected lateinit var mPaint:Paint

    var mTextChars:CharArray? = null
        private set

    private var spaceWidth = Int.MAX_VALUE //空格宽度，给到最小的一半
    private var eachNumWidth = 0
    private val normalCharMap = mutableMapOf<Char, Int>()

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        //logD { "$this onAttach to window" }
        initPaint()
        eachWidthAndHeight()
    }

    protected open fun initPaint() {
        mPaint = Paint().apply {
            setColor(mColor) // 设置画笔颜色
            textSize = mTextSize
            isAntiAlias = true // 抗锯齿
            textAlign = Paint.Align.CENTER
        }
    }

    open fun setTimeString(str:String) {
        mTextChars = str.toCharArray()
        invalidate()
    }

    /**
     * 显示之前，一定要优先调用一下，确定每个字符的宽度。
     */
    private fun eachWidthAndHeight() {
        val rect = Rect()
        var maxWidth = -1
        val chars = listOf('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')
        var minWidth = Int.MAX_VALUE
        for (ch in chars) {
            mPaint.getTextBounds("" + ch, 0, 1, rect)
            val width = rect.left + rect.right
            maxWidth = max(width, maxWidth)
            minWidth = min(minWidth, width)
            //logD { "$this width ($ch -> $width)" }
        }

        val otherChars = listOf(':', '/', '-') //由于空格不能单独统计，所以以最小的一半为准
        for (ch in otherChars) {
            mPaint.getTextBounds("" + ch, 0, 1, rect)
            val width = rect.left + rect.right
            minWidth = min(minWidth, width)
            normalCharMap[ch] = width
            //logD { "$this width ($ch -> $width)" }
        }

        eachNumWidth = maxWidth
        this.spaceWidth = minWidth / 2 //给一半即可。
        //logD { "$this maxWidth $maxWidth half $eachNumWidthHalf smallestWidth $smallestWidth" }
        //logD { "$this textSize $myTextSize" }
    }

    private val canvasRect = Rect()

    open fun getOnDrawPaintByTextIndex(index:Int, total:Int) : Paint{
        return mPaint
    }

    private fun charToWidth(ch:Char) : Int{
        if (ch in '0'..'9') {
            return eachNumWidth
        } else if (ch == ' ') {
            return spaceWidth
        } else if (normalCharMap.containsKey(ch)) {
            return normalCharMap[ch]!!
        } else {
            return eachNumWidth
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (mTextChars == null) {
            return
        }

        canvas.getClipBounds(canvasRect)
        val cHeight: Int = canvasRect.height()
        mPaint.getTextBounds("0", 0, 1, canvasRect) //我们只拿了高度，所以任意字符均可。

        //logD { "$this height $cHeight ${canvasRect.height()}" }
        val baseLine: Float = cHeight / 2f + canvasRect.height() / 2f - canvasRect.bottom
        //logD { "$this onDraw baseLineHeight y $y" }

        var startX = 0f
        val size = mTextChars?.size ?: 0
        mTextChars?.forEachIndexed { index, ch ->
            val w = charToWidth(ch)
            canvas.drawText("" + ch, startX + (w.toFloat() / 2), baseLine, getOnDrawPaintByTextIndex(index, size))
            startX += w
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        if (isInEditMode) {
            if(widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(100, (mTextSize + 0.5).toInt())
            } else if(widthSpecMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(100, heightSpecSize)
            } else if (heightSpecMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(widthSpecSize, (mTextSize + 0.5).toInt())
            }
            return
        }

        var wid = 0f
        mTextChars?.forEach { ch->
            wid += charToWidth(ch)
        }

        if(widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((wid  + 0.5).toInt(), (mTextSize + 0.5).toInt())
        } else if(widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((wid  + 0.5).toInt(), heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, (mTextSize + 0.5).toInt())
        }
    }
}