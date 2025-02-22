package com.au.module_cached.delegate

import com.au.module_android.Globals
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.IReadMoreWriteLessCacheProperty

class SharedPrefStringCache(key:String, defaultValue:String, val xmlName:String? = null) : IReadMoreWriteLessCacheProperty<String>(key, defaultValue) {
    override fun read(key: String, defaultValue: String): String {
        return SharedPrefUtil.getString(Globals.app, key, defaultValue, xmlName)
    }

    override fun save(key: String, value: String) {
        return SharedPrefUtil.putString(Globals.app, key, value, xmlName)
    }
}