package com.au.module_android.init

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.au.module_android.Globals.activityList
import java.util.concurrent.CopyOnWriteArrayList

object GlobalBackgroundCallback : DefaultLifecycleObserver {

    /**
     * 是否是在前台
     */
    val isForeground: Boolean
        get() = !isInBackground

    /**
     * 是否在后台
     */
    val isBackground:Boolean
        get() = isInBackground

    //是否后台运行
    private var isInBackground = false

    private val listeners by lazy {
        CopyOnWriteArrayList<(Boolean)->Unit>()
    }

    fun addListener(callback:(Boolean)->Unit) {
        listeners.add(callback)
    }

    fun removeListener(callback:(Boolean)->Unit) {
        listeners.remove(callback)
    }

    override fun onStart(owner: LifecycleOwner) {
        if (isInBackground) {
            isInBackground = false
        }
        notifyListener()
    }

    override fun onStop(owner: LifecycleOwner) {
        isInBackground = activityList.isNotEmpty() //ProcessLifecycleOwner.get().lifecycle监听的结果，onStop就代表进入了后台。如果应用还活着就会回调。
        if (isInBackground) {
            notifyListener()
        }
    }

    private fun notifyListener() {
        val inBg = isInBackground
        listeners.forEach {
            it(inBg)
        }
    }
}