package com.au.module_android.simplelivedata

import androidx.lifecycle.LiveData

/**
 * @author au
 * 基础的带状态的LiveData。使用它。
 */
open class StatusLiveData<T> : SafeLiveData<StatusData<T>>, IOperator<T> {
    companion object {
        fun <T> createRealDataWrap(data: T?, @Status status:Int, code: Int?, msg: String?) =
            StatusData<T>().also {
                it.data = data
                it.status = status
                it.code = code
                it.message = msg
            }
    }

    constructor()
    constructor(data:T?, @Status status:Int = Status.OVER_SUCCESS, code:Int?= null, msg:String? = null)
            : super(createRealDataWrap(data, status, code, msg))

    override fun success(data: T?, code: Int?, msg: String?) {
        setValueSafe(createRealDataWrap(data, Status.OVER_SUCCESS, code, msg))
    }

    override fun error(data: T?, code: Int?, msg: String?) {
        setValueSafe(createRealDataWrap(data, Status.OVER_ERROR, code, msg))
    }

    override fun running(data: T?, code: Int?, msg: String?) {
        setValueSafe(createRealDataWrap(data, Status.RUNNING, code, msg))
    }

    fun isRunning() = value?.code == Status.RUNNING

    fun isSuccess() = value?.code == Status.OVER_SUCCESS

    fun isError() = value?.code == Status.OVER_ERROR

    val data:T? = value?.data
}

fun <T> LiveData<T>.asStatusLiveData() = this as StatusLiveData<T>