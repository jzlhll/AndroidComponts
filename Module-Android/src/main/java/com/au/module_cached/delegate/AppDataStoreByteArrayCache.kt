package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreByteArrayCache(key:String, defaultValue:ByteArray, cacheFileName: String? = null)
    : IReadMoreWriteLessCacheProperty<ByteArray>(key, defaultValue), IDataStoreWrap by DataStoreWrap(cacheFileName) {
    override fun read(key: String, defaultValue: ByteArray): ByteArray {
        return AppDataStore.readBlocked(key, defaultValue, dataStore)
    }

    override fun save(key: String, value: ByteArray) {
        return AppDataStore.save(key, value, dataStore)
    }
}