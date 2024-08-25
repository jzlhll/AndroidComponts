package com.au.module_android.chianmode

interface IChain<T> {
    fun handle(params:T) : ChainReturn
}