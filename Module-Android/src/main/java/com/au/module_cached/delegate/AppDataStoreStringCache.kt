package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreStringCache(key:String, defaultValue:String, cacheFileName: String? = null)
        : IReadMoreWriteLessCacheProperty<String>(key, defaultValue), IDataStoreWrap by DataStoreWrap(cacheFileName){
    override fun read(key: String, defaultValue: String): String {
        return AppDataStore.readBlocked(key, defaultValue, dataStore)
    }

    override fun save(key: String, value: String) {
        return AppDataStore.save(key, value, dataStore)
    }
}