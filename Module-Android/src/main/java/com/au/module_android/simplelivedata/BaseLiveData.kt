package com.au.module_android.simplelivedata

import android.os.Looper
import androidx.lifecycle.LiveData

@Deprecated("不得直接使用。请使用StatusLiveData。")
abstract class BaseLiveData<T> : LiveData<RealDataWrap<T>>() {
    private var _wrap: RealDataWrap<T>? = null

    /**
     * 状态
     */
    @Status
    val status: Int
        get() = _wrap?.status ?: Status.NONE
    /**
     * 被包装的真实数据
     */
    val data: T?
        get() = _wrap?.data

    protected fun safeSetValue(data:T?, @Status status:Int, code:Int?= null, msg:String? = null) {
        val wrap = _wrap ?: RealDataWrap<T>().also { _wrap = it }

        if(code != null) wrap.code = code
        if(msg != null) wrap.message = msg
        if(data != null) wrap.data = data
        wrap.status = status

        if (Looper.getMainLooper() === Looper.myLooper()) {
            setValue(wrap)
        } else {
            postValue(wrap)
        }
    }
}