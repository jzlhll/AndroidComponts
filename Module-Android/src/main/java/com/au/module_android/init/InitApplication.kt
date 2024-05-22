package com.au.module_android.init

import android.app.Application

/**
 * @author au
 * @date :2023/11/7 14:32
 * @description: 使用InitApplication做为基础的application父类或者直接使用
 */
open class InitApplication : Application() {
    override fun onCreate() {
        FirstInitial.init(this)
        super.onCreate()
    }
}