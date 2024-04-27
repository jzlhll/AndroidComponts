package com.au.module_android.simplelivedata

import android.os.Looper
import androidx.lifecycle.LiveData

/**
 * 最基础的LiveData。常规使用它。
 */
open class SafeLiveData<T> : LiveData<T> {
    constructor()

    constructor(data:T?) : super(data)

    /**
     * 不论如何都放到主线程。
     */
    open fun setValueSafe(data:T?) {
        if (Looper.getMainLooper() === Looper.myLooper()) {
            setValue(data)
        } else {
            postValue(data)
        }
    }
}

fun <T> LiveData<T>.asSafeLiveData() = this as SafeLiveData<T>
