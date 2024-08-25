package com.au.module_android.utils

import com.au.module_android.Globals.gson
import com.google.gson.reflect.TypeToken

//一级泛型解析
inline fun <reified T> String.parseJson(): T {
    if (T::class.java == String::class.java) {
        return this as T
    }
    return gson.fromJson(this, object : TypeToken<T>() {}.type)
}

//二级泛型解析：使用TypeToken的版本。
inline fun <reified T, reified TLv2> String.parseJsonLv2(): T {
    if (T::class.java == String::class.java) {
        return this as T
    }
    val typeToken = TypeToken.getParameterized(T::class.java, TLv2::class.java).type
    return gson.fromJson(this, typeToken)
}