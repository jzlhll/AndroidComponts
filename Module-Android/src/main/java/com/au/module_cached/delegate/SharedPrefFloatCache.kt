package com.au.module_cached.delegate

import com.au.module_android.Globals
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.IReadMoreWriteLessCacheProperty

class SharedPrefFloatCache(key:String, defaultValue:Float, val xmlName:String? = null) : IReadMoreWriteLessCacheProperty<Float>(key, defaultValue) {
    override fun read(key: String, defaultValue: Float): Float {
        return SharedPrefUtil.getFloat(Globals.app, key, defaultValue, xmlName)
    }

    override fun save(key: String, value: Float) {
        return SharedPrefUtil.putFloat(Globals.app, key, value, xmlName)
    }
}