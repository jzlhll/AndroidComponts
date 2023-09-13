package com.au.module_android.chianmode


/**
 * 责任链模板。
 */
abstract class AbstractChain<T> : IChain<T> {
    private var filter: IChainFilter<T>? = null
    private var interrupt: IChainInterrupt<T>? = null

    var next: AbstractChain<T>? = null

    /**
     * 对于外部调用者而言，只调用链条的第一个。只关注结果是否得到了处理。
     *
     * 具体的实现需要在内部调用next的处理。
     */
    override final fun handle(params:T) : ChainReturn {
        val isInterrupt = interrupt?.interrupt(params, this) == true
        if (isInterrupt) {
            return ChainReturn.Interrupt
        }

        val isFilter = filter?.filter(params, this) == true
        if (!isFilter && selfHandle(params)) {
            return ChainReturn.True
        }

        val n = next ?: return ChainReturn.False
        return n.handle(params)
    }

    /**
     * 请实现自己本类的处理逻辑。如果接受了return true。
     */
    abstract fun selfHandle(params: T) : Boolean
}