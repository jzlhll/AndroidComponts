package com.au.module_cached.delegate

import com.au.module_android.utils.AbsRMWLCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreLongCache(key:String, defaultValue:Long) : AbsRMWLCacheProperty<Long>(key, defaultValue) {
    override fun read(key: String, defaultValue: Long): Long {
        return AppDataStore.readBlocked(key, defaultValue)
    }

    override fun save(key: String, value: Long) {
        return AppDataStore.save(key, value)
    }
}