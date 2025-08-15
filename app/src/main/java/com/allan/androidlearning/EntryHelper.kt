package com.allan.androidlearning

import android.content.Context
import com.au.module_android.utils.logd

class EntryHelper constructor(private val data:AnotherData,
                                     private val context: Context
) {
    private val activity get() = context as EntryActivity

    fun test() {
        data.data = "Another data 2025"
        logd { "test inject for test " + data.data  + " " + activity}
    }
}