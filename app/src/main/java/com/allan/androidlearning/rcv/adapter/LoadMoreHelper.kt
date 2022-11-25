package com.allan.androidlearning.rcv.adapter

import com.allan.androidlearning.rcv.ILoadMoreHelper
import com.allan.androidlearning.rcv.ILoadMoreListener

internal class LoaderMoreHelper : ILoadMoreHelper {
    /**
     * 当前状态
     */
    private var _currentStatus = STATUS_ALL_DATA_FINISHED

    override fun getCurrentStatus(): Int {
        return _currentStatus
    }

    /**
     * 监听器
     */
    private val listeners = ArrayList<ILoadMoreListener>(2)

    /**
     * 加载更多回调
     */
    override fun onLoadMore(lastPosition: Int) {
        //正在加载或者所有数据加载完成或者正在刷新，不进行任何操作
        val st = _currentStatus
        if (st == STATUS_LOAD_MORE_ING
            || st == STATUS_REFRESHING
            || st == STATUS_ALL_DATA_FINISHED
        ) {
            return
        }
        _currentStatus = STATUS_LOAD_MORE_ING
        listeners.forEach {
            it.onStatusChange(STATUS_LOAD_MORE_ING)
            it.onLoadMore()
        }
    }

    /**
     * 刷新回调
     */
    override fun onRefresh() {
        //正在刷新不进行任何操作
        val st = _currentStatus
        if (st == STATUS_REFRESHING) {
            return
        }
        _currentStatus = STATUS_REFRESHING
        listeners.forEach {
            it.onStatusChange(STATUS_REFRESHING)
            it.onRefresh()
        }
    }

    /**
     * 重置为刷新状态
     */
    override fun resetRefreshState() {
        _currentStatus = STATUS_REFRESH_FINISHED
    }

    override fun updateStatus(refresh: Boolean, hasMore: Boolean) {
        _currentStatus = if (refresh) {
            STATUS_REFRESH_FINISHED
        } else {
            STATUS_LOAD_MORE_FINISHED
        }
        listeners.forEach {
            it.onStatusChange(_currentStatus)
        }
        if (!hasMore) {
            _currentStatus = STATUS_ALL_DATA_FINISHED
            listeners.forEach {
                it.onStatusChange(_currentStatus)
            }
        }
    }

    /**
     * 添加监听
     */
    override fun addListener(l: ILoadMoreListener) {
        if (!listeners.contains(l)) {
            listeners.add(l)
        }
    }

    /**
     * 移除监听
     */
    override fun removeListener(l: ILoadMoreListener?) {
        listeners.remove(l)
    }
}