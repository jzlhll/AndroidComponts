package com.au.module_android.utils

/**
 * 忽略错误,默认不提示异常
 */
inline fun <T:Any> ignoreError(
    block: () -> T?
): T? {
    return try {
        block.invoke()
    } catch (e: Throwable) {
        e.printStackTrace()
        null
    }
}