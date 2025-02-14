package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreDoubleCache(key:String, defaultValue:Double, cacheFileName: String? = null)
    : IReadMoreWriteLessCacheProperty<Double>(key, defaultValue), IDataStoreWrap by DataStoreWrap(cacheFileName) {
    override fun read(key: String, defaultValue: Double): Double {
        return AppDataStore.readBlocked(key, defaultValue, dataStore)
    }

    override fun save(key: String, value: Double) {
        return AppDataStore.save(key, value, dataStore)
    }
}