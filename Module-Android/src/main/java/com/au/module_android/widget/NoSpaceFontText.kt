package com.au.module_android.widget

import android.content.Context
import android.graphics.Paint.FontMetricsInt
import android.graphics.Rect
import android.util.AttributeSet
import com.au.module_android.fontutil.checkBoldAndSetFont
import com.au.module_android.fontutil.setFontFromAsset
import com.google.android.material.textview.MaterialTextView

/**
 * @author au
 * Date: 2023/8/24
 */
open class NoSpaceFontText : MaterialTextView {
    private var mode: TextViewCheckMode

    var fontMode: FontMode
        get() {
            return mode.fontMode
        }
        set(value) {
            mode.fontMode = value
            setFontFromAsset(context, mode.fontMode, mode.isNumber)
            invalidate()
        }

    var isNumber:Boolean
        get() {
            return mode.isNumber
        }
        set(value) {
            mode.isNumber = value
            setFontFromAsset(context, mode.fontMode, mode.isNumber)
            invalidate()
        }

    constructor(context: Context) : super(context) {
        mode = TextViewCheckMode(FontMode.NORMAL, false)
    }
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mode = checkBoldAndSetFont(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        mode = checkBoldAndSetFont(context, attrs)
    }

    ////////////////////////参考网上的github，NoSpaceTextView
    /**
     * 控制measure()方法 刷新测量
     */
    private var refreshMeasure = false
    private val mRefreshRect = Rect()

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        removeSpace(widthMeasureSpec, heightMeasureSpec)
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        super.setText(text, type)
        // 每次文本内容改变时，需要测量两次，确保计算的高度没有问题
        refreshMeasure = true
    }

    /**
     * 这里处理文本的上下留白问题
     */
    private fun removeSpace(widthspc: Int, heightspc: Int) {
        val paddingTop: Int
        val linesText = getLinesText()
        val paint = paint
        val text = linesText[0]
        paint.getTextBounds(text, 0, text!!.length, mRefreshRect)
        val fontMetricsInt = FontMetricsInt()
        paint.getFontMetricsInt(fontMetricsInt)
        paddingTop = fontMetricsInt.top - mRefreshRect.top

        // 设置TextView向上的padding (小于0, 即把TextView文本内容向上移动)
        setPadding(
            leftPaddingOffset, paddingTop + topPaddingOffset, rightPaddingOffset, bottomPaddingOffset
        )
        val endText = linesText[linesText.size - 1]
        paint.getTextBounds(endText, 0, endText!!.length, mRefreshRect)

        // 再减去最后一行文本的底部空白，得到的就是TextView内容上线贴边的的高度，到达消除文本上下留白的问题
        setMeasuredDimension(
            measuredWidth, measuredHeight - (fontMetricsInt.bottom - mRefreshRect.bottom)
        )
        if (refreshMeasure) {
            refreshMeasure = false
            measure(widthspc, heightspc)
        }
    }

    /**
     * 获取每一行的文本内容
     */
    private fun getLinesText(): Array<String?> {
        var start = 0
        var end: Int
        val texts = arrayOfNulls<String>(lineCount)
        val text = text.toString()
        val layout = layout
        for (i in 0 until lineCount) {
            end = layout.getLineEnd(i)
            val line = text.substring(start, end) //指定行的内容
            start = end
            texts[i] = line
        }
        return texts
    }
}