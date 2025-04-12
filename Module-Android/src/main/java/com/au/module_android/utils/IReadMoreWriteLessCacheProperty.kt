package com.au.module_android.utils

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 读多写少的代理实现
 */
abstract class IReadMoreWriteLessCacheProperty<T:Any>(
    private val key: String,
    private val defaultValue: T) : ReadWriteProperty<Any?, T> {

    protected var value: T? = null

    protected abstract fun read(key: String, defaultValue: T) : T
    protected abstract fun save(key: String, value: T)

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val lastValue = value
        return if (lastValue == null) {
            val cacheValue = read(key, defaultValue)
            value = cacheValue
            cacheValue
        } else {
            lastValue
        }
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        this.value = value
        save(key, value)
    }
}