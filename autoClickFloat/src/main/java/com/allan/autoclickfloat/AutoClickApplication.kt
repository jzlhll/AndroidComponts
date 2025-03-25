package com.allan.autoclickfloat

import android.util.Log
import com.allan.autoclickfloat.activities.autofs.AutoFsObj
import com.allan.autoclickfloat.consts.Const
import com.au.module_android.InitApplication

/**
 * @author allan
 * @date :2024/4/16 17:27
 * @description:
 */
class AutoClickApplication : InitApplication() {
    override fun onCreate() {
        super.onCreate()
        Log.d(Const.TAG, "application onCreate") //这就能让他进行初始化了.所以不要去掉。
        AutoFsObj.init(this)
    }
}