package com.au.module_cached.delegate

import com.au.module_android.Globals
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.IReadMoreWriteLessCacheProperty

class SharedPrefBooleanCache(key:String, defaultValue:Boolean, val xmlName:String? = null)
        : IReadMoreWriteLessCacheProperty<Boolean>(key, defaultValue) {
    override fun read(key: String, defaultValue: Boolean): Boolean {
        return SharedPrefUtil.getBoolean(Globals.app, key, defaultValue, xmlName)
    }

    override fun save(key: String, value: Boolean) {
        return SharedPrefUtil.putBoolean(Globals.app, key, value, xmlName)
    }
}





