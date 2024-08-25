package com.au.module_android.utils

/**
 * 获取倒数第二个。如果获取不到则返回最顶。或者null
 */
fun <T> List<T>.secondLastOrNull(): T? {
    val sz = size
    if (sz >= 2) return this[sz - 2]
    if (sz > 0) return this[0]
    return null
}