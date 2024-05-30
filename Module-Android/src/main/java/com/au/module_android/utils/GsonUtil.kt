package com.au.module_android.utils

import com.au.module_android.Apps.gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

//一级泛型解析：不支持List<xxxBean>, 不支持二级嵌套xxxBean<CmdData>
inline fun <reified T> String.parseJson(): T {
    if (T::class.java == String::class.java) {
        return this as T
    }
    return gson.fromJson(this, object : TypeToken<T>() {}.type)
}

//二级泛型解析：使用TypeToken的版本。推荐。
inline fun <reified T, reified TLv2> String.parseJsonLv2(): T {
    if (T::class.java == String::class.java) {
        return this as T
    }
    val typeToken = TypeToken.getParameterized(T::class.java, TLv2::class.java).type
    return gson.fromJson(this, typeToken)
}

abstract class GsonParameterizedType<T> : ParameterizedType

//二级泛型解析：使用ParameterizedType的版本。
inline fun <reified T, reified T2> parseJsonLv2V2(jsonStr: String): T {
    return gson.fromJson(jsonStr, object : GsonParameterizedType<T>() {
        override fun getActualTypeArguments(): Array<Type> {
            return arrayOf(T2::class.java)
        }

        override fun getRawType(): Type {
            return T::class.java
        }

        override fun getOwnerType(): Type? {
            return null
        }
    })
}

//二级泛型有2个的解析：平常比较少见，我这里注释掉了。
//inline fun <reified T, reified TLv21, reified TLv22> String.parseJsonLv22(): T {
//    if (T::class.java == String::class.java) {
//        return this as T
//    }
//    val typeToken = TypeToken.getParameterized(T::class.java, TLv21::class.java, TLv22::class.java).type
//    return gson.fromJson(this, typeToken)
//}