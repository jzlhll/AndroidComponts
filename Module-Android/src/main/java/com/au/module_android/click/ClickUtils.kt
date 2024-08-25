package com.au.module_android.click

import android.view.View
import com.au.module_android.Globals.globalPaddingClickTime

/**
 * 上一次按钮点击时间
 */
private var lastClickTime = 0L

fun acceptClick(spaceTime:Long? = null) : Boolean {
    val space = spaceTime ?: globalPaddingClickTime
    val cur = System.currentTimeMillis()
    if (cur - lastClickTime < space) {
        return false
    }
    lastClickTime = cur
    return true
}

/**
 * 默认的全局设置
 */
fun View.onClick(c:(view: View)->Unit) = setOnClickListener(PaddingClickListener(globalPaddingClickTime, c))
/**
 * 默认的全局设置
 */
fun View.onClick(paddingTime:Long, c:(view: View)->Unit) = setOnClickListener(PaddingClickListener(paddingTime, c))

fun View.onTagClick(tag:Any, wrapClick:(view: View, tag:Any) ->Unit) = setOnClickListener(PaddingTagClickListener(globalPaddingClickTime, tag, wrapClick))

fun View.onTagClick(paddingTime:Long?, tag:Any, wrapClick:(view: View, tag:Any) ->Unit) = setOnClickListener(PaddingTagClickListener(paddingTime, tag, wrapClick))