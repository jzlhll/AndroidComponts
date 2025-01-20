package com.au.module_cached.delegate

import com.au.module_android.utils.AbsRMWLCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreStringCache(key:String, defaultValue:String) : AbsRMWLCacheProperty<String>(key, defaultValue) {
    override fun read(key: String, defaultValue: String): String {
        return AppDataStore.readBlocked(key, defaultValue)
    }

    override fun save(key: String, value: String) {
        return AppDataStore.save(key, value)
    }
}