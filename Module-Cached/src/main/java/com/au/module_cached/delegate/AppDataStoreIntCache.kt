package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreIntCache(key:String, defaultValue:Int) : IReadMoreWriteLessCacheProperty<Int>(key, defaultValue) {
    override fun read(key: String, defaultValue: Int): Int {
        return AppDataStore.readBlocked(key, defaultValue)
    }

    override fun save(key: String, value: Int) {
        return AppDataStore.save(key, value)
    }
}