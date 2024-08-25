package com.au.module_android.utils

import android.graphics.Outline
import android.graphics.Rect
import android.text.InputFilter
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import android.widget.EditText
import androidx.core.view.forEach

fun View.visible() {
    if(visibility != View.VISIBLE) visibility = View.VISIBLE
}

fun View.invisible() {
    if(visibility != View.INVISIBLE) visibility = View.INVISIBLE
}

fun View.gone() {
    if(visibility != View.GONE) visibility = View.GONE
}

fun View.visibleOrGone(visible:Boolean) {
    if (visible) {
        visible()
    } else {
        gone()
    }
}

fun View.visibleOrInvisible(visible:Boolean) {
    if (visible) {
        visible()
    } else {
        invisible()
    }
}

/**
 * 遍历所有子view
 */
fun View?.forEachChild(action: ((View) -> Unit)) {
    if (this == null) {
        return
    }
    action.invoke(this)
    if (this is ViewGroup) {
        this.forEach {
            it.forEachChild(action)
        }
    }
}

/**
 * 通过outlineProvider和setClipToOutline来给View设置圆角。
 */
fun View.setOutlineProviderRoundCorner(radius:Float) {
    val provider = object : ViewOutlineProvider() {
        override fun getOutline(view: View, outline: Outline) {
            val rect = Rect()
            view.getGlobalVisibleRect(rect)
            val leftMargin = 0
            val topMargin = 0
            val selfRect = Rect(
                leftMargin, topMargin,
                rect.right - rect.left - leftMargin, rect.bottom - rect.top - topMargin
            )
            outline.setRoundRect(selfRect, radius)
        }
    }

    this.outlineProvider = provider
    this.setClipToOutline(true)
}

/**
 * 设置最大长度
 */
fun EditText.setMaxLength(max: Int) {
    addFilters(InputFilter.LengthFilter(max))
}

/**
 * 所有字母大写
 */
fun EditText?.allCaps() {
    addFilters(InputFilter.AllCaps())
}

/**
 * 在之前的基础上，新增一个或者多个InputFilter
 */
fun EditText?.addFilters(vararg filter: InputFilter) {
    this ?: return
    if (filter.isNullOrEmpty()) {
        return
    }
    val old = this.filters
    val new = arrayOf<InputFilter>(*old, *filter)
    this.filters = new
}