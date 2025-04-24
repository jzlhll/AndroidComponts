package com.au.module_cached.delegate

import com.au.module_android.json.fromJson
import com.au.module_android.json.toJsonString
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 通过json string存入到AppDataStoreStringCache。实现转换Json。
 * 不能直接修改你的Class里面的内容是不会给你保存的。 你需要·等于一下·就能保存了。
 */
class AppDataStoreJsonCache<T:Any>(key:String, defaultValue:T, private val tClass:Class<T>, cacheFileName: String? = null)
        : ReadWriteProperty<Any, T> {
    private var cache by AppDataStoreStringCache(key, defaultValue.toJsonString(), cacheFileName)

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val jsonStr = cache
        return fromJson(jsonStr, tClass)
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        cache = value.toJsonString()
    }
}