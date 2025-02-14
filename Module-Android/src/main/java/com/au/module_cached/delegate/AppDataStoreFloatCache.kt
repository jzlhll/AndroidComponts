package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreFloatCache(key:String, defaultValue:Float, cacheFileName: String? = null)
    : IReadMoreWriteLessCacheProperty<Float>(key, defaultValue), IDataStoreWrap by DataStoreWrap(cacheFileName) {
    override fun read(key: String, defaultValue: Float): Float {
        return AppDataStore.readBlocked(key, defaultValue, dataStore)
    }

    override fun save(key: String, value: Float) {
        return AppDataStore.save(key, value, dataStore)
    }
}