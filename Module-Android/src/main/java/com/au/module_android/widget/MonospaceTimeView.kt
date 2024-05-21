package com.au.module_android.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
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
    private fun convertDpToPixel(dp: Float, context: Context): Float {
        val metrics = context.resources.displayMetrics
        val px = dp * (metrics.densityDpi / 160f)
        return px
    }

    var mColor = Color.BLACK
        private set

    var mTextSize = 32f //这是一个随意的值，必须通过initParamOnCreate设置。
        private set

    protected lateinit var mPaint:Paint

    var mTextChars:CharArray? = null
        private set

    private var smallestWidth = Int.MAX_VALUE
    private var eachNumWidth = 0
    private val normalCharMap = mutableMapOf<Char, Int>()

    /**
     * 在activity或者fragment的onCreate函数中调用本函数。
     * 这样在onAttach上屏的时候，才能得到结果。
     */
    open fun initParamOnCreate(textSize:Float, @ColorInt color:Int) {
        //logD { "$this initParamOnCreate" }
        mTextSize = convertDpToPixel(textSize, context)
        mColor = color
    }

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
        for (ch in chars) {
            mPaint.getTextBounds("" + ch, 0, 1, rect)
            val width = rect.left + rect.right
            maxWidth = max(width, maxWidth)
            smallestWidth = min(smallestWidth, width)
            //logD { "$this width ($ch -> $width)" }
        }

        val otherChars = listOf(':', '/')
        for (ch in otherChars) {
            mPaint.getTextBounds("" + ch, 0, 1, rect)
            val width = rect.left + rect.right
            smallestWidth = min(smallestWidth, width)
            normalCharMap[ch] = width
            //logD { "$this width ($ch -> $width)" }
        }

        eachNumWidth = maxWidth
        //logD { "$this maxWidth $maxWidth half $eachNumWidthHalf smallestWidth $smallestWidth" }
        //logD { "$this textSize $myTextSize" }
    }

    private val canvasRect = Rect()

    open fun getOnDrawPaintByTextIndex(index:Int, total:Int) : Paint{
        return mPaint
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
        val y: Float = cHeight / 2f + canvasRect.height() / 2f - canvasRect.bottom
        //logD { "$this onDraw baseLineHeight y $y" }

        var x = 0f
        val size = mTextChars?.size ?: 0
        mTextChars?.forEachIndexed { index, ch ->
            val w = charToWidth(ch)
            canvas.drawText("" + ch, x + (w.toFloat() / 2), y, getOnDrawPaintByTextIndex(index, size))
            x += w
        }
    }

    private fun charToWidth(ch:Char) : Int{
        if (ch in '0'..'9') {
            return eachNumWidth
        } else if (normalCharMap.containsKey(ch)) {
            return normalCharMap[ch]!!
        } else { //空格
            return smallestWidth shr 1
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val widthSpecMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSpecSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSpecMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSpecSize = MeasureSpec.getSize(heightMeasureSpec)

        var x = 0f
        mTextChars?.forEach { ch->
            x += charToWidth(ch)
        }

        if(widthSpecMode == MeasureSpec.AT_MOST && heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((x  + 0.5).toInt(), (mTextSize + 0.5).toInt())
        } else if(widthSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension((x  + 0.5).toInt(), heightSpecSize)
        } else if (heightSpecMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSpecSize, (mTextSize + 0.5).toInt())
        }
    }
}