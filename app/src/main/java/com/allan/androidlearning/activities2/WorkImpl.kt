package com.allan.androidlearning.activities2

import com.au.module_android.utils.logd

class WorkImpl : IWork {
    override fun doWork() {
        logd { "do work in workImpl." }
    }
}