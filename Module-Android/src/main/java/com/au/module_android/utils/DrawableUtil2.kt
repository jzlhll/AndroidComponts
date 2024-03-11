package com.au.module_android.utils

import android.R
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.annotation.StyleableRes

class ViewBackgroundBuilder {
    /**
     * Shape is a rectangle, possibly with rounded corners
     */
    val SHAPE_RECTANGLE = 0

    /**
     * Shape is an ellipse
     */
    val SHAPE_OVAL = 1

    /**
     * Shape is a line
     */
    val SHAPE_LINE = 2

    /**
     * Shape is a ring.
     */
    val SHAPE_RING = 3

    private var mShape:Int = -1
    private var mAlpha:Float = -1f
    private var mCorner:CornerRadius? = null
    private var mStrokeWidth:Int = 0
    private var mStrokeColor:Int = 0
    private var mBg:ColorStateList? = null

    /**
     * 圆角
     */
    sealed class CornerRadius {
        fun dpToFloat(dp:Float) = dp.dp
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
    fun setShape(shape:Int) : ViewBackgroundBuilder{
        mShape = shape
        return this
    }

    fun setAlpha(alpha:Float) : ViewBackgroundBuilder{
        mAlpha = alpha
        return this
    }

    fun setStroke(width:Int, color:Int) : ViewBackgroundBuilder{
        if (width > 0) {
            mStrokeWidth = width
            mStrokeColor = color
        }
        return this
    }

    fun setCornerRadius(cornerRadius: Float) : ViewBackgroundBuilder{
        mCorner = CornerRadius.AllCornerRadius(cornerRadius)
        return this
    }

    fun setCornerRadius(topLeft:Float, topRight:Float, bottomLeft:Float, bottomRight:Float) : ViewBackgroundBuilder{
        mCorner = CornerRadius.EachCornerRadius(topLeft, topRight, bottomLeft, bottomRight)
        return this
    }

    fun setBackground(color:Int, pressedColor:Int = 0, disabledColor:Int = 0)
            : ViewBackgroundBuilder {
        val colorMap = mutableListOf<Pair<IntArray, Int>>()
        val noColor = 0
        if (pressedColor != noColor) {
            colorMap.add(Pair(intArrayOf(R.attr.state_pressed), pressedColor))
        }
        if (disabledColor != noColor) {//-代表false
            colorMap.add(Pair(intArrayOf(-R.attr.state_enabled), disabledColor))
        }
        if(color != noColor) colorMap.add(Pair(intArrayOf(0), color))

        val size = colorMap.size
        val stateArray = arrayOfNulls<IntArray>(size)
        val colorArray = IntArray(size)
        colorMap.forEachIndexed { index, data ->
            stateArray[index] = data.first
            colorArray[index] = data.second
        }
        mBg = ColorStateList(stateArray, colorArray)
        return this
    }

    fun build() : Drawable {
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
            it.setStroke(mStrokeWidth, mStrokeColor)
        }

        //形状 RECTANGLE, OVAL, LINE, RING
        when (mShape) {
            0->it.shape = GradientDrawable.RECTANGLE
            1->it.shape = GradientDrawable.OVAL
            2->it.shape = GradientDrawable.LINE
            3->it.shape = GradientDrawable.RING
        }

        //alpha
        if (mAlpha in 0f..1f) {
            it.alpha = (225f * mAlpha).toInt()
        }
        return it
    }
}

/**
 * @author au
 * @date :2024/3/8 14:58
 * @description:
 */

fun View.handleCustomViewBackground(
    array: TypedArray,
    @StyleableRes _rippleColor: Int,
    @StyleableRes _unboundedRipple: Int,
    @StyleableRes _backgroundNormal: Int,
    @StyleableRes _backgroundPressed: Int,
    @StyleableRes _backgroundUnEnable: Int,
    @StyleableRes _cornerRadius: Int,
    @StyleableRes _cornerSizeTopLeft: Int,
    @StyleableRes _cornerSizeTopRight: Int,
    @StyleableRes _cornerSizeBottomLeft: Int,
    @StyleableRes _cornerSizeBottomRight: Int,
    @StyleableRes _strokeColor: Int,
    @StyleableRes _strokeWidth: Int,
    @StyleableRes _backgroundShape: Int,
    @StyleableRes _backgroundAlpha: Int,
) {
    val noColor = 0
    val rippleColor = array.getColor(_rippleColor, noColor)
    val bg = background
    if (bg != null) {
        if (rippleColor != noColor) {
            background = RippleDrawable(
                ColorStateList.valueOf(rippleColor),
                bg,
                null
            )
        }
        return
    }
    //水波纹
    val unboundedRipple = array.getBoolean(_unboundedRipple, false)
    //透明都
    val backgroundAlpha = array.getFloat(_backgroundAlpha, -1f)
    //背景
    val backgroundNormal = array.getColor(_backgroundNormal, noColor)
    val backgroundShape = array.getInt(_backgroundShape, -1)
    val backgroundPress = array.getColor(_backgroundPressed, noColor)
    val backgroundUnEnable =
        array.getColor(_backgroundUnEnable, noColor)
    val colorMap = mutableListOf<Pair<IntArray, Int>>()
    if (backgroundPress != noColor) {
        colorMap.add(Pair(intArrayOf(R.attr.state_pressed), backgroundPress))
    }
    if (backgroundUnEnable != noColor) {//-代表false
        colorMap.add(Pair(intArrayOf(-R.attr.state_enabled), backgroundUnEnable))
    }
    if (backgroundNormal != noColor) {
        colorMap.add(Pair(intArrayOf(0), backgroundNormal))
    }
    //设置边框
    val strokeColor = array.getColor(_strokeColor, noColor)
    val colorMapsize = colorMap.count()
    var rippleMaskDrawable: GradientDrawable? = null
    if (colorMapsize > 0 || strokeColor != noColor) {//设置了背景或者边框颜色
        val drawable = GradientDrawable().also {
            //region 设置颜色
            if (colorMapsize > 0) {
                val stateArray = arrayOfNulls<IntArray>(colorMapsize)
                val colorArray = IntArray(colorMapsize)
                colorMap.forEachIndexed { index, data ->
                    stateArray[index] = data.first
                    colorArray[index] = data.second
                }
                it.color = ColorStateList(stateArray, colorArray)
            } else {
                //drawable 没有颜色，导致rippleColor 无效，需要设置一个mask
                if (rippleColor != noColor) {
                    rippleMaskDrawable = GradientDrawable()
                    rippleMaskDrawable?.setColor(rippleColor)
                }
            }
            //endregion
            //region 设置圆角
            val cornerRadius = array.getDimension(_cornerRadius, -1f)
            if (cornerRadius >= 0f) {
                it.cornerRadius = cornerRadius
                rippleMaskDrawable?.cornerRadius = cornerRadius
            } else {
                val cornerSizeTopLeft =
                    array.getDimension(_cornerSizeTopLeft, 0f)
                val cornerSizeTopRight =
                    array.getDimension(_cornerSizeTopRight, 0f)
                val cornerSizeBottomLeft =
                    array.getDimension(_cornerSizeBottomLeft, 0f)
                val cornerSizeBottomRight =
                    array.getDimension(_cornerSizeBottomRight, 0f)
                if (cornerSizeTopLeft != 0f || cornerSizeTopRight != 0f || cornerSizeBottomLeft != 0f || cornerSizeBottomRight != 0f) {
                    val radii = floatArrayOf(
                        cornerSizeTopLeft, cornerSizeTopLeft,
                        cornerSizeTopRight, cornerSizeTopRight,
                        cornerSizeBottomRight, cornerSizeBottomRight,
                        cornerSizeBottomLeft, cornerSizeBottomLeft
                    )
                    it.cornerRadii = radii
                    rippleMaskDrawable?.cornerRadii = radii
                }
            }
            //endregion
            //region 设置边框
            if (strokeColor != noColor) {
                val strokeWidth = array.getDimension(_strokeWidth, 1f)
                it.setStroke(strokeWidth.toInt(), strokeColor)
            }
            //endregion
            //region 设置形状 RECTANGLE, OVAL, LINE, RING
            if (backgroundShape >= 0) {
                it.shape = backgroundShape
                rippleMaskDrawable?.shape = backgroundShape
            }
            //endregion
            if (backgroundAlpha in 0f..255f) {
                if (backgroundAlpha <= 1f) {
                    it.alpha = (225f * backgroundAlpha).toInt()
                } else {
                    it.alpha = backgroundAlpha.toInt()
                }
            }
        }
        //设置RippleDrawable
        background = if (rippleColor != noColor) {
            RippleDrawable(
                ColorStateList.valueOf(rippleColor),
                drawable,
                rippleMaskDrawable
            )
        } else {
            drawable
        }
    } else {
        if (rippleColor != noColor) {
            background = RippleDrawable(
                ColorStateList.valueOf(rippleColor),
                null,
                /*按压完之后的形状*/
                if (backgroundShape >= 0) {
                    GradientDrawable().also {
                        it.shape = backgroundShape
                        it.setColor(rippleColor)
                    }
                } else {
                    if (unboundedRipple) {
                        null
                    } else {
                        ColorDrawable(rippleColor)
                    }
                }
            )
        }
    }
}

/**
 * 对任何view设置RippleColor颜色
 */
fun View.setRippleColor(@ColorInt rippleColor: Int, radius: Int? = null) {
    val drawable = foreground ?: background
    if (drawable is RippleDrawable) {
        drawable.setColor(ColorStateList.valueOf(rippleColor))
        if (radius != null) {
            drawable.radius = radius
        }
        return
    }
    val newDrawable = RippleDrawable(
        ColorStateList.valueOf(rippleColor),
        drawable,
        if (drawable == null)
            ColorDrawable(rippleColor)
        else
            null
    )
    if (radius != null) {
        newDrawable.radius = radius
    }
    if (foreground != null) {
        foreground = newDrawable
    } else {
        background = newDrawable
    }
}