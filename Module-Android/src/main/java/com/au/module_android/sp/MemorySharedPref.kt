package com.au.module_android.sp

import android.content.Context

/**
 * 有一个情况，大量地方需要get该变量。而put该变量又需要立刻被固化起来。
 * 则通过本类，来降低sp的大量读取。
 * 写少读多的情况使用。
 *
 * T : 可选择的范围为6个。
 * Boolean, String, Int, Long, Float, Set<String>
 *     并将它传入到valueClass中。
 */
class MemorySharedPref<T:Any>(private val spName:String,
                              private val applicationContext:Context,
                              private val key:String,
                              defValue: T?,
                              private val valueClass:Class<*>) {
    private val sp by lazy { applicationContext.getSharedPreferences(spName, Context.MODE_PRIVATE) }

    private var mValue:Any? = defValue

    fun get() : T? {
        if (!sp.contains(key)) {
            return mValue as T?
        }

        //已经判断过contains，defValue则无所谓了
        when (valueClass) {
            Boolean::class.java -> {
                mValue = sp.getBoolean(key, false)
                return mValue as T?
            }
            String::class.java -> {
                mValue = sp.getString(key, null)
                return mValue as T?
            }
            Int::class.java -> {
                mValue = sp.getInt(key, 0)
                return mValue as T?
            }
            Long::class.java -> {
                mValue = sp.getLong(key, 0)
                return mValue as T?
            }
            Float::class.java -> {
                mValue = sp.getFloat(key, 0f)
                return mValue as T?
            }
            Set::class.java -> { //set String没有判断
                mValue = sp.getStringSet(key, null)
                return mValue as T?
            }
            else -> {
                throw RuntimeException("memory shared pref.")
            }
        }
    }

    fun put(value:T?) {
        this.mValue = value

        when (valueClass) {
            Boolean::class.java -> {
                if (value == null) {
                    sp.edit().remove(key).apply()
                } else {
                    sp.edit().putBoolean(key, value as Boolean).apply()
                }
            }
            String::class.java -> {
                sp.edit().putString(key, value as String?).apply()
            }
            Int::class.java -> {
                if (value == null) {
                    sp.edit().remove(key).apply()
                } else {
                    sp.edit().putInt(key, value as Int).apply()
                }
            }
            Long::class.java -> {
                if (value == null) {
                    sp.edit().remove(key).apply()
                } else {
                    sp.edit().putLong(key, value as Long).apply()
                }
            }
            Float::class.java -> {
                if (value == null) {
                    sp.edit().remove(key).apply()
                } else {
                    sp.edit().putFloat(key, value as Float).apply()
                }
            }
            Set::class.java -> { //set String没有判断
                sp.edit().putStringSet(key, value as Set<String>?).apply()
            }
        }
    }

    /**
     * 暂时不做cache。
     */
    fun contains(): Boolean {
        return sp.contains(key)
    }

    fun remove() {
        sp.edit().remove(key).apply()
    }
}