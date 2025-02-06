package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreDoubleCache(key:String, defaultValue:Double) : IReadMoreWriteLessCacheProperty<Double>(key, defaultValue) {
    override fun read(key: String, defaultValue: Double): Double {
        return AppDataStore.readBlocked(key, defaultValue)
    }

    override fun save(key: String, value: Double) {
        return AppDataStore.save(key, value)
    }
}