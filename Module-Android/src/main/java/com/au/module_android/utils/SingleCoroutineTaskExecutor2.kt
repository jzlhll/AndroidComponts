package com.au.module_android.utils
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach

/**
 * 替代HandlerThread实现的单线程协程排队工作任务队列
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SingleCoroutineTaskExecutor2 {
    @OptIn(DelicateCoroutinesApi::class)
    private val singleThreadContext = newSingleThreadContext("SequentialWorker")
    private val taskChannel = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    init {
        CoroutineScope(singleThreadContext).launch {
            taskChannel.consumeEach { task -> task() }
        }
    }

    suspend fun enqueueTask(task: suspend () -> Unit) {
        taskChannel.send(task)
    }

    fun shutdown() {
        taskChannel.close()
        singleThreadContext.close()
    }
}

//// 使用示例
//fun main() = runBlocking {
//    val executor = SequentialTaskExecutor()
//
//    repeat(5) { i ->
//        executor.enqueueTask {
//            println("Task $i start on ${Thread.currentThread().name}")
//            delay(1000) // 模拟耗时操作
//            println("Task $i end")
//        }
//    }
//
//    delay(6000)
//    executor.shutdown()
//}
