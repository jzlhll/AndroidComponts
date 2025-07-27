package com.allan.androidlearning

import android.content.Context
import com.au.module_android.utils.logd
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject

class EntryHelper @Inject constructor(private val data:AnotherData,
                                      @ActivityContext private val context: Context
) {
    private val activity get() = context as EntryActivity

    fun test() {
        data.data = "Another data 2025"
        logd { "test inject for test " + data.data  + " " + activity}
    }
}