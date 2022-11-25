package com.allan.androidlearning.rcv

interface ILoadMoreListener {
    fun onLoadMore() { }
    fun onRefresh() { }
    fun onStatusChange(status: Int) { }
}