package com.au.module_android.crossactivity

import android.os.Handler
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.BuildConfig
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.isMainThread

/**
 * @author allan
 * @date :2024/7/18 19:34
 * @description: 跨activity通知。
 * 必须把它变成一个单例（或存储在单例里面，唯一。但可以多个实例。）
 *
 * 用法在Activity或者Fragment
 * 1. onCreate()里面调用callOnCreate(this)
 * 2. 然后，给它实现ICrossNotify接口。一切就已经完成。
 *
 * 更新数据，调用changeData，必须保证是主线程。
 *
 * 内部会针对，监听的前后，分别通知不同的时机的数据。
 */
class CrossActivityNotifyListObserver<T : Any>(private val mainHandler: Handler) : DefaultLifecycleObserver {
    //value的左边代表是否监听后，有更新过。右边代表更新后的值。
    private data class CrossActivityNotifyListInfo<T>(var isResumed:Boolean,
                                              val data: ArrayList<T> = ArrayList(4))

    private val TAG = "crossNotify"

    private val callbackList = HashMap<LifecycleOwner, CrossActivityNotifyListInfo<T>>(8)

    /**
     * 如果你希望数据进行去重，比如Info(a, b, c)，其中以a字段如果相同就认为是同一份数据，
     * 本类中将会进行去重。避免多次操作同一个对象。
     */
    var distinct:((a:T, b:T)->Boolean)? = null

    /**
     * 每次resume都调用本函数。内部做了处理。
     */
    fun callOnCreate(owner: LifecycleOwner) {
        //如果不存在，就将入监听列表
        if (owner !is ICrossNotify<*>) {
            throw IllegalArgumentException("Do not call callOnCreate() because owner is not ICrossNotify!")
        }
        owner.lifecycle.addObserver(this)
        callbackList[owner] = CrossActivityNotifyListInfo(isResumed = true)
        if (BuildConfig.DEBUG) Log.d(TAG, "callOnCreate add owner $owner")
    }

    /**
     * 暂定必须，不得为空。
     * 会帮你运行在主线程。
     */
    fun notify(d:T) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "changeData $d")
        }
        if (!isMainThread) {
            mainHandler.post {
                changeDataMainThread(d)
            }
        } else {
            changeDataMainThread(d)
        }
    }

    private fun changeDataMainThread(d: T) {
        callbackList.forEach { (owner, info) ->
            addWithDistinct(info, d)

            if (info.isResumed) {
                if (BuildConfig.DEBUG) Log.d(TAG, "changeData owner $owner isResumed onDateChanged")
                owner.asOrNull<ICrossNotify<T>>()?.onCrossNotify(fetchAndClear(info), ICrossNotify.NOTIFY_WHEN_IN_RESUME)
            } else {
                if (BuildConfig.DEBUG) Log.d(TAG, "changeData owner $owner isNotResumed just save")
            }
        }
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        //如果已经存在，就把当前的通知出来。
        val info = callbackList[owner]!!
        info.isResumed = true
        if (info.data.isNotEmpty()) {
            if (BuildConfig.DEBUG) Log.d(TAG, "onResumeCall exist $owner onDataChanged ${info.data}")
            val outList = fetchAndClear(info)
            owner.asOrNull<ICrossNotify<T>>()?.onCrossNotify(outList, ICrossNotify.NOTIFY_WHEN_RESUME_POINT)
        } else {
            if (BuildConfig.DEBUG) Log.d(TAG, "onResumeCall exist $owner no change.")
        }
    }

    private fun addWithDistinct(info: CrossActivityNotifyListInfo<T>, data:T) {
        distinct?.let { dist->
            var size = info.data.size
            while (size > 0) { //倒序遍历移除
                size--
                if (dist(info.data[size], data)) {
                    info.data.removeAt(size)
                }
            }
        }

        info.data.add(data)
    }

    private fun fetchAndClear(info: CrossActivityNotifyListInfo<T>): List<T>? {
        if (info.data.isEmpty()) {
            return null
        }
        val outList = ArrayList<T>(info.data)
        info.data.clear()

        return outList
    }

    override fun onPause(owner: LifecycleOwner) {
        super.onPause(owner)
        callbackList[owner]?.isResumed = false
        if (BuildConfig.DEBUG) Log.d(TAG, "onPause owner $owner")
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        callbackList.remove(owner)
        if (BuildConfig.DEBUG) Log.d(TAG, "onDestroy owner remove $owner")
    }
}