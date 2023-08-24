package com.au.module_android.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

/**
 * 切换到主线程
 * 不创建新的协程
 *  withContext开销更低 类似于 async{..}.await()
 */
suspend inline fun <T> withMainThread(crossinline block: suspend () -> T): T {
    return if (!isMainThread) {
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
    return if (isMainThread) {
        withContext(Dispatchers.IO) {
            block.invoke()
        }
    } else {
        block.invoke()
    }
}