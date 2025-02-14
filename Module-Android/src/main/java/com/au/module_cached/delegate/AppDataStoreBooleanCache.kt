package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreBooleanCache(key:String, defaultValue:Boolean, cacheFileName: String? = null)
        : IReadMoreWriteLessCacheProperty<Boolean>(key, defaultValue), IDataStoreWrap by DataStoreWrap(cacheFileName) {
    override fun read(key: String, defaultValue: Boolean): Boolean {
        return AppDataStore.readBlocked(key, defaultValue, dataStore)
    }

    override fun save(key: String, value: Boolean) {
        return AppDataStore.save(key, value, dataStore)
    }
}