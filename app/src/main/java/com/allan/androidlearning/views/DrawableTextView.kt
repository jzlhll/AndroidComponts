package com.allan.androidlearning.views

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.graphics.drawable.Drawable
import android.content.res.TypedArray
import android.util.AttributeSet
import com.allan.androidlearning.R

/**
 * 可以自由控制 drawable 大小的 TextView
 */
class DrawableTextView : AppCompatTextView {
    private var mStartWidth: Int = 0
    private var mStartHeight: Int = 0
    private var mTopWidth: Int = 0
    private var mTopHeight: Int = 0
    private var mEndWidth: Int = 0
    private var mEndHeight: Int = 0
    private var mBottomWidth: Int = 0
    private var mBottomHeight: Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.DrawableTextView)

        mStartWidth = typedArray.getDimensionPixelOffset(R.styleable.DrawableTextView_drawableStartWidth, 0)
        mStartHeight = typedArray.getDimensionPixelOffset(R.styleable.DrawableTextView_drawableStartHeight, 0)
        mTopWidth = typedArray.getDimensionPixelOffset(R.styleable.DrawableTextView_drawableTopWidth, 0)
        mTopHeight = typedArray.getDimensionPixelOffset(R.styleable.DrawableTextView_drawableTopHeight, 0)
        mEndWidth = typedArray.getDimensionPixelOffset(R.styleable.DrawableTextView_drawableEndWidth, 0)
        mEndHeight = typedArray.getDimensionPixelOffset(R.styleable.DrawableTextView_drawableEndHeight, 0)
        mBottomWidth = typedArray.getDimensionPixelOffset(R.styleable.DrawableTextView_drawableBottomWidth, 0)
        mBottomHeight = typedArray.getDimensionPixelOffset(R.styleable.DrawableTextView_drawableBottomHeight, 0)
        typedArray.recycle()
        setDrawablesSize()
    }

    private fun setDrawablesSize() {
        val compoundDrawables = compoundDrawablesRelative
        for (i in compoundDrawables.indices) {
            when (i) {
                0 -> setDrawableBounds(compoundDrawables[0], mStartWidth, mStartHeight)
                1 -> setDrawableBounds(compoundDrawables[1], mTopWidth, mTopHeight)
                2 -> setDrawableBounds(compoundDrawables[2], mEndWidth, mEndHeight)
                3 -> setDrawableBounds(compoundDrawables[3], mBottomWidth, mBottomHeight)
                else -> {
                }
            }

        }
        setCompoundDrawablesRelative(
            compoundDrawables[0],
            compoundDrawables[1],
            compoundDrawables[2],
            compoundDrawables[3]
        )
    }

    private fun setDrawableBounds(drawable: Drawable?, width: Int, height: Int) {
        drawable?.let {
            it.setBounds(0, 0, width, height)
            if (width == 0 || height == 0) {
                val scale = it.intrinsicHeight.toDouble() / it.intrinsicWidth.toDouble()
                val bounds = it.bounds
                //高宽只给一个值时，自适应
                if (width == 0 && height != 0) {
                    bounds.right = (bounds.bottom / scale).toInt()
                    it.bounds = bounds
                }
                if (width != 0 && height == 0) {
                    bounds.bottom = (bounds.right * scale).toInt()
                    it.bounds = bounds
                }
            }
        }
    }
}