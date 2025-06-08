package com.au.module_android.widget

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import kotlin.math.max

class AutoResizeEditText : CustomEditText {
    private var isChanged = false
    private var mOrigHeight:Int? = null
    private var mLastLineCount = 1

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                isChanged = true
                requestLayout() // 触发重新测量布局
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        if (isChanged) { // 根据行数动态调整高度
            val lineCount = lineCount.coerceAtLeast(1).coerceAtMost(8)
            val origHeight = mOrigHeight
            if (origHeight != null) {
                if (mLastLineCount != lineCount) {
                    mLastLineCount = lineCount
                    setMeasuredDimension(measuredWidth, max(origHeight, lineCount * lineHeight + lineHeight / 8))
                }
            }
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (mOrigHeight == null) {
            mOrigHeight = bottom - top
        }
    }
}
