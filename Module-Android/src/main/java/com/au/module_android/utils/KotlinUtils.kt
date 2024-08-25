package com.au.module_android.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

/**
 * 切换到主线程
 * 不创建新的协程
 *  withContext开销更低 类似于 async{..}.await()
 */
suspend fun <T> withMainThread(block: suspend () -> T): T {
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

/**
 * 在io线程操作
 */
suspend inline fun <T> awaitOnIoThread(crossinline block: (CancellableContinuation<T>) -> Unit): T {
    return withIoThread {
        suspendCancellableCoroutine(block)
    }
}

fun CoroutineScope.launchOnThread(
    block: suspend CoroutineScope.() -> Unit
): Job {
    return launch(Dispatchers.Default, start = CoroutineStart.DEFAULT, block = block)
}

fun CoroutineScope.launchOnUi(
    block: suspend CoroutineScope.() -> Unit
): Job {
    return launch(Dispatchers.Main.immediate, start = CoroutineStart.DEFAULT, block = block)
}