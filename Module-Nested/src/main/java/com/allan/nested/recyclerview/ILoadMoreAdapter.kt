package com.allan.nested.recyclerview

/**
 * author: allan
 * Time: 2022/11/22
 * Desc:
 */
interface ILoadMoreAdapter<DATA> {
    /**
     * 刷新数据重载为新的一份数据。
     */
    fun initDatas(datas: List<DATA>?, hasMore: Boolean = false, isTraditionalUpdate:Boolean = false)
    /**
     * 加载更多数据
     */
    fun appendDatas(appendList: List<DATA>?, hasMore: Boolean = false)
}