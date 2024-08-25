package com.au.module_android.simplelivedata

import android.os.Looper

/**
 * @author au
 * Date: 2023/2/17
 * Description 重新设计带状态的LiveData。简化之前一堆额外代码。
 * 传入的泛型是T，监听到的是RealDataWrap。
 *
 * 携带状态的LiveData，并且更新数据必定在主线程上。
 */
open class StatusLiveData<T : Any> : NoStickLiveData<RealDataWrap<T>>(), IOperator<T> {
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

    fun setValueSafe(data:T?, @Status status:Int, code:Int?= null, msg:String? = null) {
        val wrap = _wrap ?: RealDataWrap<T>().also { _wrap = it }

        wrap.code = code
        wrap.message = msg
        wrap.data = data
        wrap.status = status

        if (Looper.getMainLooper() === Looper.myLooper()) {
            setValue(wrap)
        } else {
            postValue(wrap)
        }
    }

    override fun success(data: T?, code: Int?, msg: String?) {
        setValueSafe(data, Status.OVER_SUCCESS, code, msg)
    }

    override fun error(data: T?, code: Int?, msg: String?) {
        setValueSafe(data, Status.OVER_ERROR, code, msg)
    }

    override fun running(data: T?, code: Int?, msg: String?) {
        setValueSafe(data, Status.RUNNING, code, msg)
    }

    override fun runningOldData(code: Int?, msg: String?) {
        setValueSafe(this.data, Status.RUNNING, code, msg)
    }
}