package com.allan.autoclickfloat

import com.allan.autoclickfloat.floats.FloatingManager
import com.au.module_android.init.InitApplication

/**
 * @author allan
 * @date :2024/4/16 17:27
 * @description:
 */
class AutoClickApplication : InitApplication() {
    override fun onCreate() {
        super.onCreate()
        FloatingManager.init()
    }
}