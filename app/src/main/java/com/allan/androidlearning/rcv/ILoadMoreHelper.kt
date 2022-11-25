package com.allan.androidlearning.rcv

internal interface ILoadMoreHelper {
    fun getCurrentStatus(): Int

    /**
     * 添加监听
     */
    fun addListener(l: ILoadMoreListener)

    /**
     * 移除监听
     */
    fun removeListener(l: ILoadMoreListener?)

    fun updateStatus(refresh: Boolean, hasMore: Boolean)

    fun onLoadMore(lastPosition: Int)

    fun resetRefreshState()

    fun onRefresh()
}