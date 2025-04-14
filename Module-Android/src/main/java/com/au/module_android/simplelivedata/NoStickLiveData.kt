package com.au.module_android.simplelivedata

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.au.module_android.utils.asOrNull
import kotlin.concurrent.Volatile

/**
 * @author allan
 * @date :2024/6/20 11:20
 * @description:
 * 1. 添加支持真实的数据；
 * 2. 添加noStick(去除粘性)；
 * 3. 增加safe操作。
 */
open class NoStickLiveData<T> : LiveData<T> {
    @Volatile
    private var mRealData: Any? = null

    /**
     * 本身来讲，默认支持NoStick即可。
     */
    var isSupportNoStick: Boolean = true

    private var mVersion: Long

    /**
     * 是否有值。不能判断为null。因为第一次设置成null，也算有值。这样的话，只能判断mVersion
     */
    fun isHasValue(): Boolean {
        return mVersion >= 0
    }

    constructor(value: T) : super(value) {
        mVersion = 0
    }

    constructor() : super() {
        mVersion = -1
    }

    //////////////////////////////////
    /////// safe处理 & realValue
    //////////////////////////////////

    fun setValueSafe(value : T?) {
        if (Looper.getMainLooper() === Looper.myLooper()) {
            setValue(value)
        } else {
            postValue(value)
        }
    }

    @Deprecated("如非必要，推荐使用setValueSafe。")
    override fun setValue(value: T?) {
        mVersion++
        mRealData = value
        super.setValue(value)
    }

    @Deprecated("如非必要，推荐使用setValueSafe。")
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

    //////////////////////////////////
    /////// no stick
    //////////////////////////////////

    override fun removeObserver(observer: Observer<in T>) {
        super.removeObserver(observer)
        if(isSupportNoStick) removeObserverUnStick(observer)
    }

    /**
     * 追加不处理粘性的方式
     */
    fun observeUnStick(owner: LifecycleOwner, observer: Observer<in T>) {
        if (!isSupportNoStick) {
            throw RuntimeException("Please set isSupportNoStick = true.")
        }
        super.observe(owner, NoStickWrapObserver(this, mVersion, observer = observer))
    }

    fun observeForeverUnStick(observer: Observer<in T>) {
        super.observeForever(NoStickWrapObserver(this, mVersion, observer = observer))
    }

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

    private class NoStickWrapObserver<D>(val self: NoStickLiveData<D>,
                                         val initVersion:Long,
                                         val observer: Observer<in D>)
            : Observer<D> {
        override fun onChanged(value: D) {
            if (initVersion < self.mVersion) {
                observer.onChanged(value)
            }
        }
    }
}

inline fun <reified B> LiveData<B>.asNoStickLiveData() : NoStickLiveData<B> = this as NoStickLiveData<B>