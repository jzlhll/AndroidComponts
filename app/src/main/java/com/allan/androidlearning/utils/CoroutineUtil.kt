package com.allan.androidlearning.utils

import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 切换到主线程
 * 不创建新的协程
 *  withContext开销更低 类似于 async{..}.await()
 */
suspend inline fun <T> withMainThread(crossinline block: suspend () -> T): T {
    return if (Looper.getMainLooper() != Looper.myLooper()) {
        withContext(Dispatchers.Main.immediate) {
            block.invoke()
        }
    } else {
        block.invoke()
    }
}

/**
 * 始终在io线程运行代码
 */
suspend inline fun <T> withIoThread(crossinline block: suspend () -> T): T {
    return if (Looper.getMainLooper() != Looper.myLooper()) {
        withContext(Dispatchers.IO) {
            block.invoke()
        }
    } else {
        block.invoke()
    }
}