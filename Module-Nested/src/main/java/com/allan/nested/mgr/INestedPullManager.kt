package com.allan.nested.mgr

/**
 * @author allan.jiang
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
}