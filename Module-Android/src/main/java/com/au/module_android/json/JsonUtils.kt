package com.au.module_android.json

import com.au.module_android.Apps.gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

/**
 * 扩展：将任意对象，转成jsonString。
 * Bundle 可能会直接出错。
 */
fun Any.toJsonString() : String {
    if (isBaseType()) {
        return "" + this
    }
    return gson.toJson(this)
}

/**
 * 检查是不是基础类型
 */
private fun Any.isBaseType() : Boolean{
    when (this) {
        is Byte, Boolean, Short, Char, Float, Double, Int, Long, String -> return true
    }
    return false
}

/**
 * 扩展：将string转成任意类型的对象
 */
inline fun <reified T> String.fromJson() : T? {
    if (this.isNotEmpty()) {
        //return gson.fromJson(strJson, TypeToken<List<T>>() {}.getType());
        //改为下面的方法，clazz传入实际想要解析出来的类
        //return BaseGlobalConst.gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
        return gson.fromJson(this, TypeToken.get(T::class.java))
    }
    return null
}

/**
 * 扩展：将string转成任意类型List的对象
 */
inline fun <reified E> String.fromJsonList() : List<E>? {
    return fromJsonList(E::class.java)
}

/**
 * 扩展：将string转成任意类型List的对象
 */
fun <E> String.fromJsonList(elementClass:Class<E>) : List<E>? {
    if (this.isNotEmpty()) {
        //return gson.fromJson(strJson, TypeToken<List<T>>() {}.getType());
        //改为下面的方法，clazz传入实际想要解析出来的类
        //return BaseGlobalConst.gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
        val listType : Type = TypeToken.getParameterized(ArrayList::class.java, elementClass).type
        return gson.fromJson(this, listType)
    }
    return null
}