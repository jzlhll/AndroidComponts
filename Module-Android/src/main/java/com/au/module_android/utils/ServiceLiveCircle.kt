package com.au.module_android.utils

import android.content.Intent

interface ServiceLiveCircle {
    fun onServiceCreated()

    fun onServiceDestroy()

    fun onServiceStartCommand(intent:Intent)
}