package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreFloatCache(key:String, defaultValue:Float) : IReadMoreWriteLessCacheProperty<Float>(key, defaultValue) {
    override fun read(key: String, defaultValue: Float): Float {
        return AppDataStore.readBlocked(key, defaultValue)
    }

    override fun save(key: String, value: Float) {
        return AppDataStore.save(key, value)
    }
}