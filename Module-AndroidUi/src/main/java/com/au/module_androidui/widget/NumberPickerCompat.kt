package com.au.module_androidui.widget

import android.graphics.Typeface
import android.os.Build
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import com.au.module_android.utils.ignoreError
import java.lang.reflect.Field

/**
 * 整理常用的函数。用来通用代码。
 */
abstract class NumberPickerCompat {
    companion object {
        fun create(numberPicker: Any) : NumberPickerCompat {
            return if (numberPicker is SimpleNumberPicker) {
                SimpleNumberPickerCompat(numberPicker)
            } else {
                OrigNumberPickerCompat(numberPicker as android.widget.NumberPicker)
            }
        }
    }

    interface OnValueChangeListener {
        fun onValueChange(oldVal: Int, newVal: Int)
    }

    interface OnScrollEndListener {
        fun onScrollEnd()
    }

    abstract fun setSelectionDividerHeight(height: Int)
    abstract fun setTextSize(@FloatRange(from = 0.0, fromInclusive = false) size: Float)
    abstract fun setTextColor(@ColorInt color: Int)
    abstract fun setSecondTextColor(@ColorInt color: Int)
    abstract fun setFormatter(formatter:android.widget.NumberPicker.Formatter)
    abstract fun setWrapSelectorWheel(wrapSelectorWheel: Boolean)
    abstract fun setMaxValue(max:Int)
    abstract fun setMinValue(min:Int)
    abstract fun getValue():Int
    abstract fun setValue(v:Int)
    abstract fun setOnValueChangedListener(onValueChangedListener:OnValueChangeListener)
    abstract fun setOnScrollEnd(onScrollEndListener:OnScrollEndListener)
    abstract fun setTypeFace(typeface: Typeface);
}

class SimpleNumberPickerCompat(val numberPicker: SimpleNumberPicker) : NumberPickerCompat() {
    override fun setOnValueChangedListener(onValueChangedListener: OnValueChangeListener) {
        numberPicker.setOnValueChangedListener { _, oldVal, newVal->
            onValueChangedListener.onValueChange(oldVal, newVal)
        }
    }

    override fun getValue(): Int {
        return numberPicker.value
    }

    override fun setValue(v: Int) {
        numberPicker.value = v
    }

    override fun setMaxValue(max: Int) {
        numberPicker.maxValue = max
    }

    override fun setMinValue(min: Int) {
        numberPicker.minValue = min
    }

    override fun setTypeFace(typeface: Typeface) {
        numberPicker.setTypeFace(typeface)
    }

    override fun setWrapSelectorWheel(wrapSelectorWheel: Boolean) {
        numberPicker.wrapSelectorWheel = wrapSelectorWheel
    }

    override fun setSelectionDividerHeight(height: Int) {
        numberPicker.selectionDividerHeight = height
    }

    override fun setTextSize(size: Float) {
        numberPicker.textSize = size
    }

    override fun setTextColor(color: Int) {
        numberPicker.textColor = color
        numberPicker.setMainColor(color)
    }

    override fun setSecondTextColor(color: Int) {
        numberPicker.setSecondColor(color)
    }

    override fun setFormatter(formatter: android.widget.NumberPicker.Formatter) {
        numberPicker.setFormatter(formatter)
    }

    override fun setOnScrollEnd(onScrollEndListener: OnScrollEndListener) {
        numberPicker.setOnScrollListener { _, scrollState ->
            if (scrollState == SimpleNumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                onScrollEndListener.onScrollEnd()
            }
        }
    }
}

class OrigNumberPickerCompat(private val numberPicker: android.widget.NumberPicker) : NumberPickerCompat() {
    override fun setOnValueChangedListener(onValueChangedListener: OnValueChangeListener) {
        numberPicker.setOnValueChangedListener { _, oldVal, newVal->
            onValueChangedListener.onValueChange(oldVal, newVal)
        }
    }

    override fun getValue(): Int {
        return numberPicker.value
    }

    override fun setValue(v: Int) {
        numberPicker.value = v
    }

    override fun setMaxValue(max: Int) {
        numberPicker.maxValue = max
    }

    override fun setMinValue(min: Int) {
         numberPicker.minValue = min
    }

    override fun setTypeFace(typeface: Typeface) {
    }

    override fun setWrapSelectorWheel(wrapSelectorWheel: Boolean) {
        numberPicker.wrapSelectorWheel = wrapSelectorWheel
    }

    override fun setSelectionDividerHeight(height: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPicker.selectionDividerHeight = height
        } else {
            ignoreError {
                setNumberPickerDivider(numberPicker, height)
            }
        }
    }

    override fun setTextSize(size: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPicker.textSize = size
        }
    }

    override fun setTextColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            numberPicker.textColor = color
        }
    }

    override fun setSecondTextColor(color: Int) {
    }

    override fun setFormatter(formatter: android.widget.NumberPicker.Formatter) {
        numberPicker.setFormatter(formatter)
    }

    /**
     * 设置picker分割线的高度(分割线的粗细)
     */
    private fun setNumberPickerDivider(picker: android.widget.NumberPicker, height:Int) {
        val fields: Array<Field> = android.widget.NumberPicker::class.java.declaredFields
        for (f in fields) {
            if (f.getName().equals("mSelectionDividerHeight")) {
                f.isAccessible = true
                try {
                    f.set(picker, height)
                } catch (e: IllegalAccessException) {
                    e.printStackTrace()
                }
                break
            }
        }
    }

    override fun setOnScrollEnd(onScrollEndListener: OnScrollEndListener) {
        numberPicker.setOnScrollListener { _, scrollState ->
            if (scrollState == android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                onScrollEndListener.onScrollEnd()
            }
        }
    }
}