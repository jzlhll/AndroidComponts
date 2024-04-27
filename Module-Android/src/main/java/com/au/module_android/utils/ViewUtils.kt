package com.au.module_android.utils

import android.view.View
import android.view.ViewGroup
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