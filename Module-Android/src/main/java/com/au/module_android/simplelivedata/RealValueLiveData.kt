package com.au.module_android.simplelivedata

import androidx.lifecycle.LiveData
import kotlin.concurrent.Volatile

/**
 * @author allan
 * @date :2024/6/20 11:20
 * @description:
 * 1. 添加支持真实的数据；
 * 2. 添加noStick(去除粘性)；
 * 3. 增加safe操作。
 */
open class RealValueLiveData<T> : LiveData<T> {
    @Volatile
    private var mRealData: Any? = null
    constructor(value: T) : super(value) {
        mRealData = value
    }

    constructor() : super() {
    }

    override fun setValue(value: T?) {
        mRealData = value
        super.setValue(value)
    }

    override fun postValue(value: T?) {
        mRealData = value
        super.postValue(value)
    }

    /**
     * 真实的变量。区别与getValue。
     */
    val realValue: T?
        get() {
            val realData = mRealData ?: return null
            return realData as T
        }

    /**
     * 真实的变量。区别与getValue。
     * 代码逻辑，你自身确保它就一定不会为空的。
     */
    val realValueUnsafe:T
        get() {
            return mRealData as T
        }
}