package com.au.module_android.text

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.au.module.android.R

/**
 *
 * 描述：仅仅处理标准的颜色；背景；不做ripple的水波相关。
 * 请使用的android:background，因此customBackground过滤掉。
 *
 * 带左侧，或者右侧 图标的搜索按钮。通过自定义属性drawableXXXWidth, drawableXXXHeight来控制不同位置显示的图标。
 * todo: 目前有一个控件要求图标需要靠左，但是有padding。因此，暂时只添加了drawableMarginStart
 */
open class CustomImageText : CustomFontText {

    private var mStartWidth: Int = 0
    private var mStartHeight: Int = 0
    private var mTopWidth: Int = 0
    private var mTopHeight: Int = 0
    private var mEndWidth: Int = 0
    private var mEndHeight: Int = 0
    private var mBottomWidth: Int = 0
    private var mBottomHeight: Int = 0

    private var mDrawableMarginStart:Int = 0
    private var mDrawableMarginEnd:Int = 0

    constructor(context: Context) : super(context) {}

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomImageTextView)
        mDrawableMarginStart = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableMarginStart, 0)
        mDrawableMarginEnd = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableMarginEnd, 0)
        mStartWidth = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableStartWidth, 0)
        mStartHeight = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableStartHeight, 0)
        mTopWidth = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableTopWidth, 0)
        mTopHeight = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableTopHeight, 0)
        mEndWidth = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableEndWidth, 0)
        mEndHeight = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableEndHeight, 0)
        mBottomWidth = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableBottomWidth, 0)
        mBottomHeight = typedArray.getDimensionPixelOffset(R.styleable.CustomImageTextView_drawableBottomHeight, 0)
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
            it.setBounds(mDrawableMarginStart, 0, width + mDrawableMarginStart + mDrawableMarginEnd, height)
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