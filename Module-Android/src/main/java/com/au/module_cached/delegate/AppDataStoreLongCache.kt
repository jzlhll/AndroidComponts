package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreLongCache(key:String, defaultValue:Long, cacheFileName: String? = null)
    : IReadMoreWriteLessCacheProperty<Long>(key, defaultValue), IDataStoreWrap by DataStoreWrap(cacheFileName) {
    override fun read(key: String, defaultValue: Long): Long {
        return AppDataStore.readBlocked(key, defaultValue, dataStore)
    }

    override fun save(key: String, value: Long) {
        return AppDataStore.save(key, value, dataStore)
    }
}