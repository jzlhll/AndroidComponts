package com.au.module_cached.delegate

import com.au.module_android.utils.AbsRMWLCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreFloatCache(key:String, defaultValue:Float) : AbsRMWLCacheProperty<Float>(key, defaultValue) {
    override fun read(key: String, defaultValue: Float): Float {
        return AppDataStore.readBlocked(key, defaultValue)
    }

    override fun save(key: String, value: Float) {
        return AppDataStore.save(key, value)
    }
}