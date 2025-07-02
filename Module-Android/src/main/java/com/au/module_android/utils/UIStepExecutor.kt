package com.au.module_android.utils

import android.os.Handler

// 步骤执行器
class UIStepExecutor(private val uiHandler: Handler,
                     vararg steps: ()->Unit,
) {
    var mSteps = steps

    // 开始执行
    fun start() {
        executeNextStep()
    }

    private var index = 0
    // 执行下一步
    private fun executeNextStep() {
        if (index >= mSteps.size) {
            return
        }
        val step = mSteps[index++]
        uiHandler.post {
            step()
            executeNextStep()
        }
    }

}