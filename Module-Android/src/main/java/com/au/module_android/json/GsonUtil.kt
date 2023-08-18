package com.au.module_android.json

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


/**
 * E是列表item的类型
 */
fun <E> mmkvSetArrayList(key:String, value:List<E>) {
    val json = BaseGlobalConst.gson.toJson(value)
    kv.putString(key, json)
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

    val json = BaseGlobalConst.mmkv.getString(key, "")
    if (!json.isNullOrEmpty()) {
        //return gson.fromJson(strJson, TypeToken<List<T>>() {}.getType());
        //改为下面的方法，clazz传入实际想要解析出来的类
        //return BaseGlobalConst.gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
        val listType : Type = TypeToken.getParameterized(ArrayList::class.java, elementClass).type
        return BaseGlobalConst.gson.fromJson(json, listType)
    }

    return retList
}