package com.au.module.cached

import android.os.Parcelable
import com.au.module_android.Globals.app
import com.au.module_android.Globals.gson
import com.google.gson.reflect.TypeToken
import com.tencent.mmkv.MMKV
import java.lang.reflect.Type

/**
 * 腾讯的数据存储库
 */
val mmkv by lazy { MMKV.initialize(app);MMKV.defaultMMKV() }

/**
 * E是列表item的类型
 */
fun mmkvSetArrayList(key:String, value:List<*>) {
    val json = gson.toJson(value)
    mmkv.putString(key, json)
}

/**
 * E是列表item的类型
 */
inline fun <reified E> mmkvGetArrayList(key:String) : ArrayList<E> {
    return mmkvGetArrayList(key, E::class.java)
}

/**
 * E是列表item的类型
 */
fun <E> mmkvGetArrayList(key:String, elementClass:Class<E>) : ArrayList<E> {
    val retList = ArrayList<E>()

    val json = mmkv.getString(key, "")
    if (!json.isNullOrEmpty()) {
        //return gson.fromJson(strJson, TypeToken<List<T>>() {}.getType());
        //改为下面的方法，clazz传入实际想要解析出来的类
        //return BaseGlobalConst.gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
        val listType : Type = TypeToken.getParameterized(ArrayList::class.java, elementClass).type
        return gson.fromJson(json, listType)
    }

    return retList
}


/**
 * E是列表item的类型
 */
fun mmkvSetMap(key:String, value:Map<*, *>) {
    val json = gson.toJson(value)
    mmkv.putString(key, json)
}

/**
 * E是列表item的类型
 */
inline fun <reified K, reified V> mmkvGetMap(key:String) : Map<K, V> {
    return mmkvGetMap(key, K::class.java, V::class.java)
}

/**
 * E是列表item的类型
 */
fun <K, V> mmkvGetMap(key:String, keyClass:Class<K>, valueClass:Class<V>) : Map<K, V> {
    val json = mmkv.getString(key, "")
    if (!json.isNullOrEmpty()) {
        val listType : Type = TypeToken.getParameterized(Map::class.java, keyClass, valueClass).type
        return gson.fromJson(json, listType)
    }

    return emptyMap()
}

/**
 * 通过mmkv保存任意数据。当然并非任意。
 * 主要是基础类型+ByteArray+parcelable+一些数据Bean类型通过gson来转存。
 * 大约90%的case。特殊的自行处理。
 *
 */
fun mmkvSetAny(key:String, value:Any) {
    when (value) {
        is String -> mmkv.putString(key, value)
        is Int -> mmkv.putInt(key, value)
        is Long -> mmkv.putLong(key, value)
        is Boolean -> mmkv.putBoolean(key, value)
        is Double -> mmkv.encode(key, value)
        is Float -> mmkv.putFloat(key, value)
        is ByteArray -> mmkv.encode(key, value)
        is Parcelable -> mmkv.encode(key, value)
        else -> {
            mmkv.putString(key, gson.toJson(value))
        }
    }
}

/**
 * 通过mmkv获取，非常规的数据类型。
 */
inline fun <reified T> mmkvGet(key:String) : T? {
    return mmkvGet(key, T::class.java)
}

/**
 * 通过mmkv获取，非常规的数据类型。
 */
fun <T> mmkvGet(key:String, tClass:Class<*>) : T? {
    val str = mmkv.getString(key, null) ?: return null
    val type : Type = TypeToken.getParameterized(tClass).type
    return gson.fromJson<T>(str, type)
}

fun mmkvForceSync() {
    mmkv.async()
}