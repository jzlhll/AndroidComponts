package com.allan.autoclickfloat.taks

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

/**
 * @author allan.jiang
 * Date: 2023/6/25
 * Description: timeout的辅助类。帮助Fragment|Activity处理超时的代码
 *
 * 可以直接在init或者全局变量初始化即可。不用临时创建。
 */
class LifeCycleCountDowner(private val owner: LifecycleOwner, private val offset:Long = 0) {
    private var mJob: Job? = null

    /**
     * 设置timeout的代码
     */
    var endAction:((reason:String)->Unit)? = null
    var countDowningAction:((leftTimeStr:String)->Unit)? = null

    /**
     * 将毫秒数动态转换为时间字符串
     *
     * @param millis 剩余时间（毫秒）
     * @return 格式化的时间字符串，例如 "01:30:45" 或 "30:45" 或 "45"
     */
    fun fmtLeftTimeStr(millis: Long): String {
        val isMinus = millis < 0
        val ms = abs(millis)

        // 计算时、分、秒
        val hours = ms / (1000 * 60 * 60) // 总小时数
        val minutes = (ms % (1000 * 60 * 60)) / (1000 * 60) // 剩余分钟数
        val seconds = (ms % (1000 * 60)) / 1000 // 剩余秒数

        val prefix = if (!isMinus) "剩余" else "已完成"
        return if (hours > 0) {
            // 超过 1 小时，显示 "时:分:秒"
            String.format("$prefix %02d时%02d分%02d秒", hours, minutes, seconds)
        } else if (minutes > 0) {
            // 超过 1 分钟但不足 1 小时，显示 "分:秒"
            String.format("$prefix %02d分%02d秒", minutes, seconds)
        } else {
            // 不足 1 分钟，显示 "秒"
            String.format("$prefix %02d 秒", seconds)
        }
    }

    /**
     * 开始delay。先cancel掉。
     */
    fun start(targetTimeTs:Long) {
        mJob?.cancel()
        mJob = owner.lifecycleScope.launch {
            do {
                val left = targetTimeTs - System.currentTimeMillis()
                val offsetLeft = left + offset
                if (offsetLeft <= 0) {
                    break
                }
                countDowningAction?.invoke(fmtLeftTimeStr(left))
                delay(999)
            } while(true)
            mJob = null
            endAction?.invoke("")
        }
    }

    /**
     * 取消delay任务。
     */
    fun cancel() {
        mJob?.cancel()
        mJob = null
    }

    fun cancelAndTrigger(reason:String) {
        mJob?.cancel()
        mJob = null
        endAction?.invoke(reason)
    }
}