package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreIntCache(key:String, defaultValue:Int, cacheFileName: String? = null)
    : IReadMoreWriteLessCacheProperty<Int>(key, defaultValue), IDataStoreWrap by DataStoreWrap(cacheFileName) {
    override fun read(key: String, defaultValue: Int): Int {
        return AppDataStore.readBlocked(key, defaultValue, dataStore)
    }

    override fun save(key: String, value: Int) {
        return AppDataStore.save(key, value, dataStore)
    }
}