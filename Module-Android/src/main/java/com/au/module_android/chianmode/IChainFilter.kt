package com.au.module_android.chianmode

/**
 * 链条的过滤器。过滤器则是阻止整个链条上的拦截自身，往下继续传播处理。
 */
interface IChainFilter<T> {
    fun filter(param:T, baseChain: AbstractChain<*>) :Boolean
}