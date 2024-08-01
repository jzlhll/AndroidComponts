package com.au.module_android.crossactivity

import androidx.annotation.MainThread

interface ICrossNotify<T> {
    @MainThread
    fun onCrossNotify(data:List<T>?)
}