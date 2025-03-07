package com.allan.autoclickfloat.taks

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * @author allan.jiang
 * Date: 2023/6/25
 * Description: timeout的辅助类。帮助Fragment|Activity处理超时的代码
 *
 * 可以直接在init或者全局变量初始化即可。不用临时创建。
 */
class LifeCycleDelayer(private val owner: LifecycleOwner) {
    private var delayJob: Job? = null

    /**
     * 设置timeout的代码
     */
    var delayAction:((reason:String)->Unit)? = null

    /**
     * 开始delay。先cancel掉。
     */
    fun startDelay(timeoutTime:Long) {
        delayJob?.cancel()
        delayJob = owner.lifecycleScope.launch {
            delay(timeoutTime)
            delayAction?.invoke("timeout")
            delayJob = null
        }
    }

    /**
     * 取消delay任务。
     */
    fun cancel() {
        delayJob?.cancel()
        delayJob = null
    }

    /**
     * 取消的同时，触发一下delayAction。
     * 请注意：如果已经不存在job了。则不会触发。
     */
    fun cancelAndTrigger(reason:String) {
        if (delayJob != null) {
            cancel()
            delayAction?.invoke(reason)
        }
    }
}