package com.au.module_android.simplelivedata

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.au.module_android.utils.ReflectionUtils
import com.au.module_android.utils.asOrNull

/**
 * @author au
 * 不需要的粘性状态。
 */
open class NoStickLiveData<T : Any> : SafeLiveData<T> {
    private var mVersion:Int

    //反射拿到父类的field mObservers。
    private var mObservers:(java.lang.Iterable<java.util.Map.Entry<*, *>>)? = null

    private fun requireMObservers() : java.lang.Iterable<java.util.Map.Entry<*, *>> {
        val mOb = mObservers
        if (mOb == null) {
            var superClass: Class<*>? = javaClass.superclass
            while (superClass != null && superClass != Any::class.java) {
                if (superClass == LiveData::class.java) {
                    val field = superClass.getDeclaredField("mObservers")
                    field.isAccessible = true
                    val o = field.get(this)
                    mObservers = o as java.lang.Iterable<java.util.Map.Entry<*, *>>
                    break
                }
                superClass = superClass.superclass
            }
            return mObservers!!
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

    override fun removeObserver(observer: Observer<in T>) {
        super.removeObserver(observer)
        removeObserverUnStick(observer)
    }

    /**
     * 追加不处理粘性的方式
     */
    fun observeUnStick(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, NoStickWrapObserver(this, observer = observer))
    }

    fun observeForeverUnStick(observer: Observer<in T>) {
        super.observeForever(NoStickWrapObserver(this, observer = observer))
    }

    fun removeObserverUnStick(observer: Observer<*>) {
        requireMObservers().let { iter->
            val foundList = ArrayList<Observer<in T>>()
            for (entry in iter) {
                val wrap = entry.key as? NoStickWrapObserver<*>
                if (wrap != null && wrap.observer == observer) {
                    wrap.asOrNull<Observer<in T>>()?.apply {
                        foundList.add(this)
                    }
                }
            }
            foundList.forEach {
                super.removeObserver(it)
            }
        }
    }

    private class NoStickWrapObserver<D:Any>(val self:NoStickLiveData<D>,
                                             val observer: Observer<in D>) : Observer<D> {
        private val version: Int = self.mVersion //标记进入的时候的版本

        override fun onChanged(value: D) {
            if (version < self.mVersion) {
                observer.onChanged(value)
            }
        }
    }
}