package com.au.module_android.utils

import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Parcelable
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.Serializable

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
 * 将异步代码写成同步调用示例
 */
suspend inline fun <T> awaitAny(crossinline block: (CancellableContinuation<T>) -> Unit): T {
    return suspendCancellableCoroutine(block)
}

/**
 * 在io线程操作
 */
suspend inline fun <T> awaitOnIoThread(crossinline block: (CancellableContinuation<T>) -> Unit): T {
    return withIoThread {
        suspendCancellableCoroutine(block)
    }
}

fun <T> unsafeLazy(initializer: () -> T) = lazy(LazyThreadSafetyMode.NONE, initializer)

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

inline fun <reified T : Serializable> Bundle.serializableCompat(key: String): T? = when {
    VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializable(key, T::class.java)
    else -> getSerializable(key) as? T
}

inline fun <reified T : Serializable> Intent.serializableExtraCompat(key: String): T? = when {
    VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> getSerializableExtra(key, T::class.java)
    else -> getSerializableExtra(key) as? T
}

inline fun <reified T : Parcelable> Intent.parcelableArrayExtraCompat(key: String): Array<T>? {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableArrayExtra<T>(key, T::class.java)
    }
    return getParcelableArrayExtra(key) as? Array<T>
}

inline fun <reified T : Parcelable> Intent.parcelableExtraCompat(key: String): T? {
    if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        return getParcelableExtra<T>(key, T::class.java)
    }
    return getParcelableExtra(key) as? T
}