package com.au.module.cached

import com.au.module_android.Globals
import com.tencent.mmkv.MMKV

/**
 * 腾讯的数据存储库
 */
val mmkv by lazy {
    MMKV.initialize(Globals.app)
    MMKV.defaultMMKV()
}