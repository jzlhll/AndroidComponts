package com.au.module_android.utils

import android.app.Activity
import android.app.Dialog
import androidx.fragment.app.Fragment
import com.au.module_android.Globals.app


/**
 * 类型转换
 */
inline fun <reified Obj> Any?.asOrNull(): Obj? {
    return if (this is Obj) {
        this
    } else {
        null
    }
}

/**
 * 根据当前界面的状态拿dp，更加合适。因为一些缩放框架会改变density
 */
fun Activity.dp(value:Float):Float {
    return value * this.resources.displayMetrics.density
}

/**
 * 根据当前界面的状态拿dp，更加合适。因为一些缩放框架会改变density
 */
fun Fragment.dp(value:Float):Float {
    return value * requireActivity().resources.displayMetrics.density
}
/**
 * 根据当前界面的状态拿dp，更加合适。因为一些缩放框架会改变density
 */
fun Dialog.dp(value:Float):Float {
    return value * this.context.resources.displayMetrics.density
}

/**
 * 如果能使用Activity.dp或者Fragment.dp则使用另外2个。
 */
fun dpGlobal(value:Float):Int {
    return (value * app.resources.displayMetrics.density).toInt()
}