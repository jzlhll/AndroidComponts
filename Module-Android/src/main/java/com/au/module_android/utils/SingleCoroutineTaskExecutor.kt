package com.au.module_android.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.internal.wait
import java.util.concurrent.Executors

class SingleCoroutineTaskExecutor(threadName: String) {
    // 1. 创建单线程执行器（命名线程）
    private val executor = Executors.newSingleThreadExecutor { r ->
        Thread(r, threadName).apply { isDaemon = true }
    }

    // 2. 转换为协程调度器
    private val dispatcher: CoroutineDispatcher = executor.asCoroutineDispatcher()

    // 3. 创建协程作用域（绑定到单线程调度器）
    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    /**
     * 添加任务到队列（按添加顺序执行）
     * @param block 要执行的任务代码块
     */
    fun submit(block: suspend () -> Unit) {
        scope.launch {
            block()
        }
    }

    /**
     * 添加任务到队列（按添加顺序执行）
     * @param block 要执行的任务代码块
     * 会得到执行结果
     */
    suspend fun <T> submitWithResult(block: suspend () -> T) : T{
        val deferred = scope.async {
            block()
        }
        return deferred.await()
    }

    /**
     * 关闭任务队列（停止接受新任务）
     */
    fun shutdown() {
        scope.coroutineContext.cancel() // 取消所有协程
        executor.shutdown()             // 关闭线程池
    }
}

// 使用示例
//fun main() = runBlocking {
//    // 创建单线程任务队列（线程名为 "WorkerThread"）
//    val taskQueue = SingleThreadTaskQueue("WorkerThread")
//
//    // 添加任务（将按顺序执行）
//    repeat(5) { i ->
//        taskQueue.post {
//            delay(100) // 模拟耗时操作
//            println("任务 $i 在 ${Thread.currentThread().name} 执行")
//        }
//    }
//
//    // 添加一个带返回值的任务
//    val deferred = CompletableDeferred<Int>()
//    taskQueue.post {
//        delay(200)
//        println("计算任务执行")
//        deferred.complete(42)
//    }
//
//    // 等待结果
//    val result = deferred.await()
//    println("计算结果: $result")
//
//    // 等待所有任务完成（实际项目中根据需要调用）
//    delay(1000)
//
//    // 关闭队列
//    taskQueue.shutdown()
//}