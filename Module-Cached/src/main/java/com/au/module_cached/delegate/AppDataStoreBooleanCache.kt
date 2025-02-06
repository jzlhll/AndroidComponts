package com.au.module_cached.delegate

import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
import com.au.module_cached.AppDataStore

class AppDataStoreBooleanCache(key:String, defaultValue:Boolean) : IReadMoreWriteLessCacheProperty<Boolean>(key, defaultValue) {
    override fun read(key: String, defaultValue: Boolean): Boolean {
        return AppDataStore.readBlocked(key, defaultValue)
    }

    override fun save(key: String, value: Boolean) {
        return AppDataStore.save(key, value)
    }
}