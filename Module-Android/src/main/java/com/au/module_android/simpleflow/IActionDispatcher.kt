package com.au.module_android.simpleflow

/**
 * 让ViewModel实现
 */
interface IActionDispatcher {
    /**
     * 界面逻辑调用
     */
    fun dispatch(action: IStateAction)

    /**
     * 获取ActionStore
     * 在ViewModel的init函数中调用getActionStore().reduce(clazz, block)
     */
    fun getActionStore(): VMActionStore
}