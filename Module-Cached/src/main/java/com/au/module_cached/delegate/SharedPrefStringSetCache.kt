package com.au.module_cached.delegate

import com.au.module_android.Globals
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.IReadMoreWriteLessCacheProperty

class SharedPrefStringSetCache(key:String, defaultValue:Set<String>) : IReadMoreWriteLessCacheProperty<Set<String>>(key, defaultValue) {
    override fun read(key: String, defaultValue: Set<String>): Set<String> {
        return SharedPrefUtil.getStringSet(Globals.app, key, defaultValue)
    }

    override fun save(key: String, value: Set<String>) {
        return SharedPrefUtil.putStringSet(Globals.app, key, value)
    }
}