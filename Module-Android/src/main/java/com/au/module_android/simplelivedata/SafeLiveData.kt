package com.au.module_android.simplelivedata

import android.os.Looper
import androidx.lifecycle.LiveData

open class SafeLiveData<T> : LiveData<T> {
    /**
     * Creates a MutableLiveData initialized with the given `value`.
     *
     * @param value initial value
     */
    constructor(value: T) : super(value)

    /**
     * Creates a MutableLiveData with no value assigned to it.
     */
    constructor() : super()

    @Deprecated("please call safeSetValue", ReplaceWith("super.postValue(value)", "androidx.lifecycle.LiveData"))
    override fun postValue(value: T) {
        super.postValue(value)
    }

    @Deprecated("please call safeSetValue", ReplaceWith("super.postValue(value)", "androidx.lifecycle.LiveData"))
    override fun setValue(value: T) {
        super.setValue(value)
    }

    fun safeSetValue(value : T) {
        if (Looper.getMainLooper() === Looper.myLooper()) {
            setValue(value)
        } else {
            postValue(value)
        }
    }
}