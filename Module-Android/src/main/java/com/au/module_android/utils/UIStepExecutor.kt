package com.au.module_android.utils

import android.os.Handler


// 步骤执行器
class UIStepExecutor(private val uiHandler: Handler) {
    // 步骤抽象类
    abstract class Step {
        internal fun executeWith(executor: UIStepExecutor) {
            onExecute()
            executor.executeNextStep()
        }

        // 抽象方法，子类实现具体逻辑
        protected abstract fun onExecute()
    }

    private val stepQueue = ArrayDeque<Step>()
    private var currentStep: Step? = null

    // 添加步骤到队列
    fun addStep(step: Step) = apply {
        stepQueue.addLast(step)
    }

    // 批量添加步骤
    fun addSteps(vararg steps: Step) = apply {
        steps.forEach { stepQueue.addLast(it) }
    }

    // 开始执行
    fun start() {
        executeNextStep()
    }

    // 执行下一步
    fun executeNextStep() {
        if (stepQueue.isEmpty()) {
            currentStep = null
            return
        }

        currentStep = stepQueue.removeFirst()
        uiHandler.post {
            currentStep?.executeWith(this)
        }
    }

}