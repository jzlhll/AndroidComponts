package com.au.module_nested.mgr

/**
 * @author au
 * Date: 2023/2/27
 */
interface INestedPullManager {
    fun loadingData():Boolean
    fun pullDownIsTargetTranslated():Boolean

    /**
     * 不论成功失败都应该调用。这样才能停止刷新和结束拦截点击事件。
     */
    fun refreshCompleted()

    /**
     * 设置刷新函数，如果不设置，或者设置为null，则变成fake模式。
     */
    fun setOnRefreshAction(onRefreshAction:(()->Unit)?)

    /**
     * 添加额外的indicator的位移Hold偏差值
     */
    fun setIndicatorDeltaHoldY(delta:Float) {}
}