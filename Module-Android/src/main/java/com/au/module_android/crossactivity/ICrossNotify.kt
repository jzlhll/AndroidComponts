package com.au.module_android.crossactivity

import androidx.annotation.MainThread

interface ICrossNotify<T> {
    companion object {
        /**
         * 当前页面就是resume状态下
         */
        const val NOTIFY_WHEN_IN_RESUME = 1

        /**
         * 当resume那一刻收到
         */
        const val NOTIFY_WHEN_RESUME_POINT = 2
    }
    @MainThread
    fun onCrossNotify(data:List<T>?, notifyWhen:Int)
}