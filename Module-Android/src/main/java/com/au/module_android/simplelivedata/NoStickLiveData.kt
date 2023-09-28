package com.au.module_android.simplelivedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.au.module_android.utils.ReflectionUtils
import com.au.module_android.utils.asOrNull

class NoStickLiveData<T> : MutableLiveData<T> {
    private var mVersion:Int

    //发射拿到父类的field
    private var mObservers:Iterable<MutableMap.MutableEntry<Observer<*>, *>>? = null

    constructor() {
        mVersion = -1
        mObservers = ReflectionUtils.iteratorGetPrivateFieldValue(this, "mObservers").asOrNull()
    }
    constructor(data:T?) : super(data) {
        mVersion = 0
        mObservers = ReflectionUtils.iteratorGetPrivateFieldValue(this, "mObservers").asOrNull()
    }

    override fun setValue(value: T?) {
        super.setValue(value)
        mVersion++
    }

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, NoStickObserverWrap(this, observer = observer))
    }

    override fun observeForever(observer: Observer<in T>) {
        super.observeForever(NoStickObserverWrap(this, observer = observer))
    }

    override fun removeObserver(observer: Observer<in T>) {
        val wrap = findObserverWrap(observer) ?: return
        super.removeObserver(wrap)
    }

    private fun findObserverWrap(observer: Observer<in T>) : Observer<in T>? {
        mObservers?.let { iter->
            for ((key, _) in iter) {
                val noStickObserverWrap = key as NoStickObserverWrap<*>
                if (noStickObserverWrap.observer == observer) {
                    return noStickObserverWrap as Observer<in T>
                }
            }
        }
        return null
    }

    private class NoStickObserverWrap<T>(val self:NoStickLiveData<T>,
                                         val observer: Observer<in T>) : Observer<T> {
        private val version: Int = self.mVersion //标记进入的时候的版本

        override fun onChanged(t: T?) {
            if (version < self.mVersion) {
                observer.onChanged(t)
            }
        }
    }

}