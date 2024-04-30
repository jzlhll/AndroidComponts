package com.au.module.cached

import android.content.Context
import com.au.module_android.utils.asOrNull
import java.util.concurrent.ConcurrentHashMap

/**
 * 有一个情况，大量地方需要get该变量。而put该变量又需要立刻被固化起来。
 * 则通过本类，来降低sp的大量读取。
 * 写少读多的情况使用。
 */
class MemorySharedPreference(private val spName:String, private val applicationContext:Context) {
    private val sp by lazy { applicationContext.getSharedPreferences(spName, Context.MODE_PRIVATE) }

    //暂时内存结果。避免过度read
    private val map = ConcurrentHashMap<String?, Any?>(4)

    fun getString(key: String, defValue: String?): String? {
        if (map.containsKey(key)) {
            return map[key].asOrNull()
        }
        return sp.getString(key, defValue).also { map[key] = it }
    }

    fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String>? {
        if (map.containsKey(key)) {
            return map[key].asOrNull()
        }
        return sp.getStringSet(key, defValues).also { map[key] = it }
    }

    /**
     * 可以将String转成int。
     */
    fun getInt(key: String, defValue: Int): Int? {
        if (map.containsKey(key)) {
            return map[key].asOrNull()
        }
        return sp.getInt(key, defValue).also { map[key] = it }
    }

    fun getLong(key: String, defValue: Long): Long {
        if (map.containsKey(key)) {
            return map[key].asOrNull() ?: 0
        }
        return sp.getLong(key, defValue).also { map[key] = it }
    }

    fun getFloat(key: String, defValue: Float): Float {
        if (map.containsKey(key)) {
            return map[key].asOrNull() ?: 0f
        }
        return sp.getFloat(key, defValue).also { map[key] = it }
    }

    fun getBoolean(key: String, defValue: Boolean): Boolean {
        if (map.containsKey(key)) {
            return map[key].asOrNull() ?: false
        }
        return sp.getBoolean(key, defValue).also { map[key] = it }
    }

    fun contains(key: String): Boolean {
        if (map.containsKey(key)) {
            return true
        }
        return sp.contains(key)
    }

    fun putString(key: String, value: String?) {
        map[key] = value
        sp.edit().putString(key, value).apply()
    }

    fun putStringSet(key: String, values: MutableSet<String>?) {
        map[key] = values
        sp.edit().putStringSet(key, values).apply()
    }

    fun putInt(key: String, value: Int) {
        map[key] = value
        sp.edit().putInt(key, value).apply()
    }

    fun putLong(key: String, value: Long) {
        map[key] = value
        sp.edit().putLong(key, value).apply()
    }

    fun putFloat(key: String, value: Float) {
        map[key] = value
        sp.edit().putFloat(key, value).apply()
    }

    fun putBoolean(key: String, value: Boolean) {
        map[key] = value
        sp.edit().putBoolean(key, value).apply()
    }

    fun remove(key: String) {
        map.remove(key)
        sp.edit().remove(key).apply()
    }

    fun clear() {
        map.clear()
        sp.edit().clear().apply()
    }
}