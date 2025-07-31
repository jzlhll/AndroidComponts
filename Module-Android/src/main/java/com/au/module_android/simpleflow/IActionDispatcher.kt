package com.au.module_android.simpleflow

/**
 * 让ViewModel实现
 */
interface IActionDispatcher {
    /**
     * 可以使用flowStateApi来执行；并设置到Flow上
     * 给外部调用
     */
    fun dispatch(action: IStateAction)
}