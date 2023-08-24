package com.au.module_android.simplelivedata

/**
 * @author allan.jiang
 * Date: 2023/2/17
 * Description 重新设计带状态的LiveData。简化之前一堆额外代码。
 * 传入的泛型是T，监听到的是RealDataWrap。
 *
 * 携带状态的LiveData，并且更新数据必定在主线程上。
 */
open class StatusLiveData<T> : BaseLiveData<T>(), IOperator<T> {
    override fun success(data: T?, code: Int?, msg: String?) {
        safeSetValue(data, Status.OVER_SUCCESS, code, msg)
    }

    override fun error(data: T?, code: Int?, msg: String?) {
        safeSetValue(data, Status.OVER_ERROR, code, msg)
    }

    override fun running(data: T?, code: Int?, msg: String?) {
        safeSetValue(data, Status.RUNNING, code, msg)
    }
}