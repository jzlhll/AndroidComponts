package com.au.module_android.simplelivedata

/**
 * @author allan
 * 带状态的不需要粘性的LiveData。
 */
class StatusNoStickLiveData<T> : NoStickLiveData<StatusData<T>?>, IOperator<T> {
    constructor()
    constructor(data:StatusData<T>) : super(data)

    override fun success(data: T?, code: Int?, msg: String?) {
        setValueSafe(StatusLiveData.createRealDataWrap(data, Status.OVER_SUCCESS, code, msg))
    }

    override fun error(data: T?, code: Int?, msg: String?) {
        setValueSafe(StatusLiveData.createRealDataWrap(data, Status.OVER_ERROR, code, msg))
    }

    override fun running(data: T?, code: Int?, msg: String?) {
        setValueSafe(StatusLiveData.createRealDataWrap(data, Status.RUNNING, code, msg))
    }
}