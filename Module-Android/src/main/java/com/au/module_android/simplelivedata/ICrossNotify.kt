package com.au.module_android.simplelivedata

import androidx.annotation.MainThread

interface ICrossNotify<T> {
    @MainThread
    fun onCrossNotify(data:List<T>?)
}