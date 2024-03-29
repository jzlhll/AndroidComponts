package com.au.module_android.simplelivedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.au.module_android.utils.ReflectionUtils

/**
 * @author au
 * 不需要的粘性状态。
 */
open class NoStickLiveData<T> : SafeLiveData<T> {
    private var mVersion:Int

    //反射拿到父类的field mObservers。
    private var mObservers:(Iterable<MutableMap.MutableEntry<Observer<*>, *>>)? = null

    private fun requireMObservers() : Iterable<MutableMap.MutableEntry<Observer<*>, *>> {
        val mOb = mObservers
        if (mOb == null) {
            val ob = ReflectionUtils.iteratorGetPrivateFieldValue(this, "mObservers", true)
            val nob = ob as Iterable<MutableMap.MutableEntry<Observer<*>, *>>
            mObservers = nob
            return nob
        }
        return mOb
    }

    constructor() {
        mVersion = -1
    }
    constructor(data:T?) : super(data) {
        mVersion = 0
    }

    override fun setValue(value: T?) {
        mVersion++
        super.setValue(value)
    }

    /**
     * 追加不处理粘性的方式
     */
    fun observeUnStick(owner: LifecycleOwner, observer: Observer<in T?>) {
        super.observe(owner, NoStickWrapObserver(this, observer = observer))
    }

    fun observeForeverUnStick(observer: Observer<in T?>) {
        super.observeForever(NoStickWrapObserver(this, observer = observer))
    }

    fun removeObserverUnStick(observer: Observer<in T?>) {
        val wrap = findObserverWrap(observer) ?: return
        super.removeObserver(wrap)
    }

    private fun findObserverWrap(observer: Observer<in T?>) : Observer<in T?>? {
        requireMObservers().let { iter->
            for ((key, _) in iter) {
                val wrap = key as NoStickWrapObserver<*>
                if (wrap.observer == observer) {
                    return wrap as Observer<in T?>
                }
            }
        }
        return null
    }

    private class NoStickWrapObserver<T>(val self:NoStickLiveData<T>,
                                         val observer: Observer<in T?>) : Observer<T?> {
        private val version: Int = self.mVersion //标记进入的时候的版本

        override fun onChanged(t: T?) {
            if (version < self.mVersion) {
                observer.onChanged(t)
            }
        }
    }

}