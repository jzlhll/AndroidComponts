package com.au.module_android.utils

import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.annotation.ColorInt
import com.au.module_android.R
import androidx.core.graphics.drawable.toDrawable

class ViewBackgroundBuilder {
    //private var mShape:Int = -1
    //private var mAlpha:Float = -1f
    private var mCorner: CornerRadius? = null
    private var mStrokeWidth:Float = 0f
    private var mStrokeColor:Int = 0
    private var mBg:ColorStateList? = null

    private var mBgAlpha = -1

    var isAtLeastOne = false

    /**
     * 圆角
     */
    sealed class CornerRadius {
        class AllCornerRadius(val size: Float) : CornerRadius()
        class EachCornerRadius(val topLeft:Float, val topRight:Float, val bottomLeft:Float, val bottomRight:Float) : CornerRadius() {
            fun convert() = floatArrayOf(
                topLeft, topLeft,
                topRight, topRight,
                bottomLeft, bottomLeft,
                bottomRight, bottomRight
            )
        }
    }

    /**
     * 0~3 RECT, OVAL, LINE, RING
     */
//    fun setShape(shape:Int) : ViewBackgroundBuilder {
//        mShape = shape
//        return this
//    }
//
//    fun setAlpha(alpha:Float) : ViewBackgroundBuilder {
//        mAlpha = alpha
//        return this
//    }

    fun setStroke(width:Float, color:Int) : ViewBackgroundBuilder {
        if (width > 0) {
            mStrokeWidth = width
            mStrokeColor = color
            isAtLeastOne = true
        }
        return this
    }

    fun setCornerRadius(cornerRadius: Float) : ViewBackgroundBuilder {
        if (cornerRadius > 0) {
            mCorner = CornerRadius.AllCornerRadius(cornerRadius)
            isAtLeastOne = true
        }
        return this
    }

    fun setCornerRadius(topLeft:Float, topRight:Float, bottomLeft:Float, bottomRight:Float) : ViewBackgroundBuilder {
        if (topLeft > 0f || topRight > 0f || bottomLeft > 0f || bottomRight > 0f) {
            mCorner = CornerRadius.EachCornerRadius(topLeft, topRight, bottomLeft, bottomRight)
            isAtLeastOne = true
        }
        return this
    }

    fun setBackgroundAlpha(alpha:Int): ViewBackgroundBuilder {
        mBgAlpha = alpha
        return this
    }

    fun setBackground(color:Int, pressedColor:Int = 0, disabledColor:Int = 0)
            : ViewBackgroundBuilder {
        val colorMap = mutableListOf<Pair<IntArray, Int>>()
        val noColor = 0

        var hasColor = false
        if (pressedColor != noColor) {
            colorMap.add(Pair(intArrayOf(android.R.attr.state_pressed), pressedColor))
            hasColor = true
        }
        if (disabledColor != noColor) {//-代表false
            colorMap.add(Pair(intArrayOf(-android.R.attr.state_enabled), disabledColor))
            hasColor = true
        }
        if(color != noColor) {
            colorMap.add(Pair(intArrayOf(0), color))
            hasColor = true
        }

        if (hasColor) {
            val size = colorMap.size
            val stateArray = arrayOfNulls<IntArray>(size)
            val colorArray = IntArray(size)
            colorMap.forEachIndexed { index, data ->
                stateArray[index] = data.first
                colorArray[index] = data.second
            }
            mBg = ColorStateList(stateArray, colorArray)

            isAtLeastOne = true
        }

        return this
    }

    fun build() : Drawable? {
        if (!isAtLeastOne) {
            return null
        }

        val it = GradientDrawable()
        //背景
        if(mBg != null) it.color = mBg

        //圆角
        when (mCorner) {
            is CornerRadius.AllCornerRadius -> {
                it.cornerRadius = (mCorner as CornerRadius.AllCornerRadius).size
            }
            is CornerRadius.EachCornerRadius -> {
                it.cornerRadii = (mCorner as CornerRadius.EachCornerRadius).convert()
            }
            null -> {}
        }

        //边框
        if (mStrokeWidth > 0 && mStrokeColor != 0) {
            it.setStroke(mStrokeWidth.toInt(), mStrokeColor)
        }

        //形状 RECTANGLE, OVAL, LINE, RING
//        when (mShape) {
//            0->it.shape = GradientDrawable.RECTANGLE
//            1->it.shape = GradientDrawable.OVAL
//            2->it.shape = GradientDrawable.LINE
//            3->it.shape = GradientDrawable.RING
//        }

        //alpha
        if (mBgAlpha >= 0) {
            it.alpha = mBgAlpha
        }
        return it
    }
}

fun View.viewBackgroundBuild(array:TypedArray) {
    val builder = ViewBackgroundBuilder()

    val noColor = 0

    val bgAlpha = array.getFloat(R.styleable.AnyView_backgroundAlpha, -1f)
    if (bgAlpha in 0f..255f) {
        val alpha = if (bgAlpha <= 1f) {
            (225f * bgAlpha).toInt()
        } else {
            bgAlpha.toInt()
        }
        builder.setBackgroundAlpha(alpha)
    }

    val bgNormalColor = array.getColor(R.styleable.AnyView_backgroundNormal, noColor)
    val bgDisabledColor = array.getColor(R.styleable.AnyView_backgroundDisabled, noColor)
    val bgPressedColor = array.getColor(R.styleable.AnyView_backgroundPressed, noColor)
    builder.setBackground(bgNormalColor, bgPressedColor, bgDisabledColor)

    val cornerRadius = array.getDimension(R.styleable.AnyView_cornerRadius, -1f)
    if (cornerRadius < 0f) {
        val cornerSizeTopLeft =
            array.getDimension(R.styleable.AnyView_cornerSizeTopLeft, 0f)
        val cornerSizeTopRight =
            array.getDimension(R.styleable.AnyView_cornerSizeTopRight, 0f)
        val cornerSizeBottomLeft =
            array.getDimension(R.styleable.AnyView_cornerSizeBottomLeft, 0f)
        val cornerSizeBottomRight =
            array.getDimension(R.styleable.AnyView_cornerSizeBottomRight, 0f)
        builder.setCornerRadius(cornerSizeTopLeft, cornerSizeTopRight, cornerSizeBottomLeft, cornerSizeBottomRight)
    } else {
        builder.setCornerRadius(cornerRadius)
    }

    val strokeColor = array.getColor(R.styleable.AnyView_strokeColor, noColor)
    val strokeWidth = array.getDimension(R.styleable.AnyView_strokeWidth, 0f)

    builder.setStroke(strokeWidth, strokeColor)
    if (builder.isAtLeastOne) {
        background = builder.build()
    }
}

/**
 * 对任何view设置RippleColor颜色
 */
fun Drawable?.setRippleColor(@ColorInt rippleColor: Int, radius: Int? = null) : Drawable{
    if (this is RippleDrawable) {
        this.setColor(ColorStateList.valueOf(rippleColor))
        if (radius != null) {
            this.radius = radius
        }
        return this
    }
    val newDrawable = RippleDrawable(
        ColorStateList.valueOf(rippleColor),
        this,
        if (this == null)
            rippleColor.toDrawable()
        else
            null
    )
    if (radius != null) {
        newDrawable.radius = radius
    }
    return newDrawable
}

/**
 * 对任何view设置RippleColor颜色
 */
fun View.setRippleColor(@ColorInt rippleColor: Int, radius: Int? = null) {
    val drawable = foreground ?: background
    val fixDrawable = drawable.setRippleColor(rippleColor, radius)
    if (foreground != null) {
        foreground = fixDrawable
    } else {
        background = fixDrawable
    }
}