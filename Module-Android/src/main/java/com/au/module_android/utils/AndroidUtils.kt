package com.au.module_android.utils

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.ContextWrapper
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import com.au.module_android.Globals
import com.au.module_android.Globals.app
import java.math.BigDecimal
import java.math.RoundingMode

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

    return value.dp
}

/**
 * 如果能使用Activity.dp或者Fragment.dp则使用另外2个。
 */
val Float.dp:Float
    get() = (this * app.resources.displayMetrics.density)

val Float.dpInt:Int
    get() = (this * app.resources.displayMetrics.density).toInt()

/**
 * 如果能使用Activity.dp或者Fragment.dp则使用另外2个。
 */
val Int.dp:Int
    get() = (this.toFloat() * app.resources.displayMetrics.density).toInt()

/**
 * 如果能使用Activity.dp或者Fragment.dp则使用另外2个。
 */
val Int.dpFloat:Float
    get() = this.toFloat() * app.resources.displayMetrics.density


/**
 * 获取缓存大小
 */
suspend fun getAppCacheSize(): String {
    return withIoThread {
        val cacheDir = Globals.app.cacheDir.getDirSize()
        val externalCacheDir = Globals.app.externalCacheDir.getDirSize()
        (cacheDir + externalCacheDir).formatLength()
    }
}

/**
 * 字节转为kb
 */
fun Long?.formatLength(): String {
    val size = this?.toFloat() ?: return "0MB"
    return when {
        size < 1024 * 1024 * 1024 -> {//不足1g
            "${(size / 1024 / 1024).keepTwoPoint()}MB"
        }
        else -> {
            "${(size / 1024 / 1024 / 1024).keepTwoPoint()}GB"
        }
    }
}

/**
 * 保留两位小数
 */
fun Float?.keepTwoPoint(roundingMode: RoundingMode = RoundingMode.HALF_EVEN): String {
    this ?: return "0.00"
    return this.toString().keepTwoPoint()
}

/**
 * 保留两位小数
 */
fun String?.keepTwoPoint(roundingMode: RoundingMode = RoundingMode.HALF_EVEN): String {
    return try {
        if (this == null) {
            "0.00"
        } else {
            BigDecimal(this).setScale(2, roundingMode)?.toString() ?: this
        }
    } catch (e: Throwable) {
        "0.00"
    }
}