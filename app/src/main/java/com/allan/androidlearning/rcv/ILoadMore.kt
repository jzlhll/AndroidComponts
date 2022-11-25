package com.allan.androidlearning.rcv

interface ILoadMore<DATA> {
    /**
     * 刷新数据重载为新的一份数据。
     */
    fun initDatas(datas: List<DATA>?, hasMore: Boolean = false)
    /**
     * 加载更多数据
     */
    fun appendDatas(appendList: List<DATA>?, hasMore: Boolean = false)
}