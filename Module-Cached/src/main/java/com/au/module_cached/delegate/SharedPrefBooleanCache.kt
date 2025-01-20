package com.au.module_cached.delegate

import com.au.module_android.Globals
import com.au.module_android.sp.SharedPrefUtil
import com.au.module_android.utils.AbsRMWLCacheProperty

class SharedPrefBooleanCache(key:String, defaultValue:Boolean) : AbsRMWLCacheProperty<Boolean>(key, defaultValue) {
    override fun read(key: String, defaultValue: Boolean): Boolean {
        return SharedPrefUtil.getBoolean(Globals.app, key, defaultValue)
    }

    override fun save(key: String, value: Boolean) {
        return SharedPrefUtil.putBoolean(Globals.app, key, value)
    }
}





