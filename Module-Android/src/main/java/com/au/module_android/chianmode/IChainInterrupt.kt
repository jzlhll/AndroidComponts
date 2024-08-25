package com.au.module_android.chianmode

/**
 * 链条的拦截器。而是直接停止链条的传播，直接停止。
 */
interface IChainInterrupt<T> {
    fun interrupt(param:T, baseChain: AbstractChain<*>) : Boolean
}