package com.allan.autoclickfloat

import android.util.Log
import com.allan.autoclickfloat.activities.autofs.AutoFsObj
import com.allan.autoclickfloat.consts.Const
import com.au.module_android.InitApplication
import com.au.module_android.utils.logd

/**
 * @author allan
 * @date :2024/4/16 17:27
 * @description:
 */
class AutoClickApplication : InitApplication() {
    override fun onCreate() {
        super.onCreate()
        //这就能让Const进行初始化.所以不要去掉。
        logd { Const.TAG + "autoClick: application onCreate" }
        AutoFsObj.init(this)
    }
}