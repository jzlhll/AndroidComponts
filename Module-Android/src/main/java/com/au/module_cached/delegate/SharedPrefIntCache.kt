package com.au.module_cached.delegate

import com.au.module_android.Globals
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.IReadMoreWriteLessCacheProperty
class SharedPrefIntCache(key:String, defaultValue:Int, val xmlName:String? = null) : IReadMoreWriteLessCacheProperty<Int>(key, defaultValue) {
    override fun read(key: String, defaultValue: Int): Int {
        return SharedPrefUtil.getInt(Globals.app, key, defaultValue, xmlName)
    }

    override fun save(key: String, value: Int) {
        return SharedPrefUtil.putInt(Globals.app, key, value, xmlName)
    }
}