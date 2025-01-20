package com.au.module_cached.delegate

import com.au.module_android.utils.AbsRMWLCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreDoubleCache(key:String, defaultValue:Double) : AbsRMWLCacheProperty<Double>(key, defaultValue) {
    override fun read(key: String, defaultValue: Double): Double {
        return AppDataStore.readBlocked(key, defaultValue)
    }

    override fun save(key: String, value: Double) {
        return AppDataStore.save(key, value)
    }
}