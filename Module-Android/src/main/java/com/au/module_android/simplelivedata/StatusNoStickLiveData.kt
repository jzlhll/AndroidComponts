package com.au.module_android.simplelivedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.au.module_android.utils.ReflectionUtils
import com.au.module_android.utils.asOrNull

/**
 * @author allan.jiang
 * Date: 2023/2/17
 * Description 重新设计带状态的LiveData。简化之前一堆额外代码。
 * 传入的泛型是T，监听到的是RealDataWrap。
 *
 * 携带状态的LiveData，并且更新数据必定在主线程上。
 */
class StatusNoStickLiveData<T> : StatusLiveData<T> {
    private var mVersion:Int

    //发射拿到父类的field
    private var mObservers:Iterable<MutableMap.MutableEntry<Observer<*>, *>>? = null

    constructor() {
        mVersion = -1
        mObservers = ReflectionUtils.iteratorGetPrivateFieldValue(this, "mObservers").asOrNull()
    }
    constructor(data:T?, @Status status:Int = Status.OVER_SUCCESS, code:Int = 0, msg:String? = null)
        : super(data, status, code, msg) {
        mVersion = 0
        mObservers = ReflectionUtils.iteratorGetPrivateFieldValue(this, "mObservers").asOrNull()
    }

    override fun setValue(value: RealDataWrap<T>?) {
        super.setValue(value)
        mVersion++
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in RealDataWrap<T>>) {
        super.observe(owner, NoStickObserverWrap(this, observer = observer))
    }

    override fun observeForever(observer: Observer<in RealDataWrap<T>>) {
        super.observeForever(NoStickObserverWrap(this, observer = observer))
    }

    override fun removeObserver(observer: Observer<in RealDataWrap<T>>) {
        val wrap = findObserverWrap(observer) ?: return
        super.removeObserver(wrap)
    }

    private fun findObserverWrap(observer: Observer<in RealDataWrap<T>>) : Observer<in RealDataWrap<T>>? {
        mObservers?.let { iter->
            for ((key, _) in iter) {
                val noStickObserverWrap = key as NoStickObserverWrap<*>
                if (noStickObserverWrap.observer == observer) {
                    return noStickObserverWrap as Observer<in RealDataWrap<T>>
                }
            }
        }
        return null
    }

    private class NoStickObserverWrap<T>(val self:StatusNoStickLiveData<T>,
                                         val observer: Observer<in RealDataWrap<T>>) : Observer<RealDataWrap<T>> {
        private val version: Int = self.mVersion //标记进入的时候的版本

        override fun onChanged(t: RealDataWrap<T>?) {
            if (version < self.mVersion) {
                observer.onChanged(t)
            }
        }
    }

}