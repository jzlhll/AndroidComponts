package com.au.module_android.utils

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import org.json.JSONArray
import org.json.JSONObject

private fun Bundle.putAny(key:String, value:Any, isThrowException: Boolean = true) { //按照优先级重新排布了一下。
    when (value) {
        is String -> putString(key, value)
        is Int -> putInt(key, value)
        is Long -> putLong(key, value)
        is Boolean -> putBoolean(key, value)
        is Double -> putDouble(key, value)
        is Float -> putFloat(key, value)

        is DoubleArray -> putDoubleArray(key, value)
        is FloatArray -> putFloatArray(key, value)
        is IntArray -> putIntArray(key, value)
        is LongArray -> putLongArray(key, value)

        // Reference arrays
        is Array<*> -> {
            val componentType = value::class.java.componentType!!
            @Suppress("UNCHECKED_CAST") // Checked by reflection.
            when {
                String::class.java.isAssignableFrom(componentType) -> putStringArray(key, value as Array<String>)
                CharSequence::class.java.isAssignableFrom(componentType) -> putCharSequenceArray(key, value as Array<CharSequence>)
                else -> {
                    val valueType = componentType.canonicalName
                    if (isThrowException) {
                        throw IllegalArgumentException(
                            "Illegal value array type $valueType"
                        )
                    } else {
                        Log.e("allan", "Illegal value array type $valueType")
                    }
                }
            }
        }
        is List<*> -> {
            if (isThrowException) {
                throw IllegalArgumentException("Illegal value type List please use Array.")
            } else {
                Log.e("allan", "Illegal value type List please use Array.")
            }
        }
        // Last resort. Also we must check this after Array<*> as all arrays are serializable.
        //is Serializable -> putSerializable(key, value)

        is Bundle -> putBundle(key, value)
        is Byte -> putByte(key, value)
        is Char -> putChar(key, value)
        is Short -> putShort(key, value)
        // References
        is CharSequence -> putCharSequence(key, value)
        //is IBinder -> putBinder(key, value)
        //is Parcelable -> putParcelable(key, value)
        // Scalar arrays
        is BooleanArray -> putBooleanArray(key, value)
        is ByteArray -> putByteArray(key, value)
        is CharArray -> putCharArray(key, value)
        is ShortArray -> putShortArray(key, value)

        else -> {
            val valueType = value.javaClass.canonicalName
            if (isThrowException) {
                throw IllegalArgumentException("Illegal value type $valueType ")
            } else {
                Log.e("allan", "Illegal value type $valueType ")
            }
        }
    }
}

/**
 * 将jsonStr转成android的Bundle格式。
 * 目前是一种很粗糙的处理。
 * 备注：子结构jsonArray和jsonObject直接转成string填入。
 */
private fun jsonObjectToBundle(jsonObject: JSONObject) : Bundle {
    val keys = jsonObject.keys()

    val pairs = mutableListOf<Pair<String, Any?>>()
    keys.forEach {
        val v = jsonObject.get(it)
        if (v is JSONArray) {
            pairs.add(Pair(it, v.toString()))
        } else if (v is JSONObject) {
            pairs.add(Pair(it, v.toString()))
        } else {
            pairs.add(Pair(it, v))
        }
    }

    return bundleOf(*pairs.toTypedArray())
}

/**
 * 将HashMap转成android的Bundle格式。
 * 基于自己编写的putAny，基本可以达到99%覆盖。
 */
private fun mapToBundle(map: Map<String, Any>) : Bundle {
    val bundle = Bundle()
    for ((key, value) in map){
        bundle.putAny(key, value)
    }
    return bundle
}