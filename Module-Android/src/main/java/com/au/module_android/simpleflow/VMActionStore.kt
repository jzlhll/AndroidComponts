package com.au.module_android.simpleflow
/**
 * 让ViewModel实现
 */
class VMActionStore {
    private val reducers = mutableMapOf<Class<out IStateAction>, (Any)->Unit>()

    /**
     * 可以使用stateRequest来执行；并设置到Flow上
     * 给外部调用
     */
    fun dispatch(action: IStateAction) {
        val reducer = reducers[action::class.java] ?: return
        reducer(action)
    }

    fun <ACTION: IStateAction> reduce(actionClass: Class<ACTION>, reducerBlock:(ACTION)->Unit) {
        reducers[actionClass] = reducerBlock as (Any) -> Unit
    }
}