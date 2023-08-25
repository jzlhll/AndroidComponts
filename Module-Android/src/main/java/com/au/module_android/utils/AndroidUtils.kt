package com.au.module_android.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.au.module_android.Globals.app

val isMainThread: Boolean
    get() = Looper.getMainLooper() === Looper.myLooper()

/**
 * 类型转换
 */
inline fun <reified T> Any?.asOrNull(): T? {
    return if (this is T) {
        this
    } else {
        null
    }
}

val View.activity: Activity?
    get() {
        val ctx = context
        if (ctx is Activity) {
            return ctx
        }
        if (ctx is ContextWrapper) {
            return ctx.baseContext.asOrNull()
        }
        return null
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

fun Context.dp(value:Float) : Float {
    if (this is Activity) {
        return this.dp(value)
    }

    return value.dpGlobal
}

/**
 * 如果能使用Activity.dp或者Fragment.dp则使用另外2个。
 */
val Float.dpGlobal:Float
    get() = (this * app.resources.displayMetrics.density)
