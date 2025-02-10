package com.au.module_nested.recyclerview.page

import com.au.module_android.simplelivedata.Status
import com.au.module_android.simplelivedata.StatusLiveData;
/**
 * @author au
 * @date :2023/10/12 10:12
 * @description: 如果调用了success，则会标记为加载成功过。这样的话，错误来的话，就不是首次出错了。
 */
class PageStatusLiveData<T : Any> : StatusLiveData<T>() {
    private var isSuccessOnce = false

    override fun success(data: T?, code: Int?, msg: String?) {
        isSuccessOnce = true
        setValueSafe(data, Status.OVER_SUCCESS, code, msg)
    }

    override fun error(data: T?, code: Int?, msg: String?) {
        if (isSuccessOnce) {
            setValueSafe(data, Status.OVER_ERROR, code, msg)
        } else {
            setValueSafe(data, Status.PAGE_INIT_ERROR, code, msg)
        }
    }

    fun isPageInitError() = value?.code == Status.PAGE_INIT_ERROR
}