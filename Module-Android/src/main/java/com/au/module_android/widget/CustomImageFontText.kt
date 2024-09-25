package com.au.module_android.widget

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import com.au.module_android.R

open class CustomImageFontText : CustomFontText {
    private var mStartWidth: Int = 0
    private var mStartHeight: Int = 0
    private var mTopWidth: Int = 0
    private var mTopHeight: Int = 0
    private var mEndWidth: Int = 0
    private var mEndHeight: Int = 0
    private var mBottomWidth: Int = 0
    private var mBottomHeight: Int = 0

    private var mDrawablePadding:Int = 0

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.CustomImageFontText)
        mDrawablePadding = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableTextPadding, 0)

        mStartWidth = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableStartWidth, 0)
        mStartHeight = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableStartHeight, 0)
        mTopWidth = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableTopWidth, 0)
        mTopHeight = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableTopHeight, 0)

        mEndWidth = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableEndWidth, 0)
        mEndHeight = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableEndHeight, 0)
        mBottomWidth = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableBottomWidth, 0)
        mBottomHeight = typedArray.getDimensionPixelOffset(R.styleable.CustomImageFontText_drawableBottomHeight, 0)
        typedArray.recycle()
        setDrawablesSize()

        compoundDrawablePadding = mDrawablePadding
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
        //避免循环调用自己
        super.setCompoundDrawablesRelative(
            compoundDrawables[0],
            compoundDrawables[1],
            compoundDrawables[2],
            compoundDrawables[3]
        )
    }

    private fun setDrawableBounds(drawable: Drawable?, width: Int, height: Int) {
        drawable?.let {
            val fixWidth:Int?
            val fixHeight:Int?
            if (width <= 0 && height <= 0) {
                fixWidth = it.intrinsicWidth
                fixHeight = it.intrinsicHeight
            } else if (width > 0 && height > 0) {
                fixWidth = width
                fixHeight = height
            } else { //&& height <= 0
                throw IllegalAccessException("You do not set width and height both!")
            }
            it.setBounds(0, 0, fixWidth, fixHeight)
        }
    }

    override fun setCompoundDrawablesRelative(start: Drawable?, top: Drawable?, end: Drawable?, bottom: Drawable?) {
        super.setCompoundDrawablesRelative(start, top, end, bottom)
        setDrawablesSize()
    }
}