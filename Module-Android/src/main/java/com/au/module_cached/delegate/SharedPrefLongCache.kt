package com.au.module_cached.delegate

import com.au.module_android.Globals
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
class SharedPrefLongCache(key:String, defaultValue:Long, val xmlName:String? = null) : IReadMoreWriteLessCacheProperty<Long>(key, defaultValue) {
    override fun read(key: String, defaultValue: Long): Long {
        return SharedPrefUtil.getLong(Globals.app, key, defaultValue, xmlName)
    }

    override fun save(key: String, value: Long) {
        return SharedPrefUtil.putLong(Globals.app, key, value, xmlName)
    }
}