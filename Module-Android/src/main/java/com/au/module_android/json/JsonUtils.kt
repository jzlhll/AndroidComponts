package com.au.module_android.json

import android.util.Log
import com.au.module_android.Globals.gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import kotlin.jvm.Throws
import kotlin.math.min

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
@Throws(JsonSyntaxException::class)
inline fun <reified T> String.fromJson() : T? {
    //return gson.fromJson(strJson, TypeToken<List<T>>() {}.getType());
    //改为下面的方法，clazz传入实际想要解析出来的类
    //return BaseGlobalConst.gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
    try {
        return gson.fromJson(this, TypeToken.get(T::class.java))
    } catch (e:JsonSyntaxException) {
        e.printStackTrace()
    }
    return null
}

/**
 * 扩展：将string转成任意类型List的对象
 */
inline fun <reified E> String.fromJsonList() : List<E> {
    return fromJsonList(E::class.java)
}

/**
 * 扩展：将string转成任意类型List的对象
 */
fun <E> String.fromJsonList(elementClass:Class<E>) : List<E> {
    //return gson.fromJson(strJson, TypeToken<List<T>>() {}.getType());
    //改为下面的方法，clazz传入实际想要解析出来的类
    //return BaseGlobalConst.gson.fromJson(json, object : TypeToken<List<T>>() {}.type)
    try {
        val listType : Type = TypeToken.getParameterized(ArrayList::class.java, elementClass).type
        return gson.fromJson(this, listType)
    } catch (e:JsonSyntaxException) {
        e.printStackTrace()
    }
    return emptyList()
}

private fun formatJsonBeautifulJsonStr(str:String) : String {
    return try {
        val jsonElement: JsonElement = JsonParser.parseString(str)
        val beautifulGson = GsonBuilder().setPrettyPrinting().create()
        return beautifulGson.toJson(jsonElement)
    } catch (e:Exception) {
        str
    }
}

fun formatJsonBeautiful(obj:Any) : String {
    if (obj is String) {
        return formatJsonBeautifulJsonStr(obj)
    }
    return try {
        val beautifulGson = GsonBuilder().setPrettyPrinting().create()
        return beautifulGson.toJson(obj)
    } catch (e:Exception) {
        ""
    }
}

fun logLargeLine(tag:String, str:String) {
    val len = str.length
    val maxLine = 300
    var i = 0
    while (i < len) {
        var lineIndex = str.indexOf("\n", i + maxLine)
        if (lineIndex == -1) {
            lineIndex = len
        }
        val log = str.substring(i, min(lineIndex, len))
        Log.d(tag, log)
        i = lineIndex + 1
    }
}