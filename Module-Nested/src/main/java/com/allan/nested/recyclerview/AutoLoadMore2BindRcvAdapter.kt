package com.allan.nested.recyclerview

import com.allan.nested.recyclerview.viewholder.BindViewHolder

/**
 * author: allan
 * Time: 2022/11/22
 * Desc: 基于AutoLoadMoreBindRcvAdapter：再次添加底部的loadingItemView。
 *
 * 传入一个static final的空DATA，当做loading的item使用。本类中进行处理。
 * 自行处理onCreateItemView, onBindItemView的loadingMore ViewType逻辑。
 *
 * todo: 目前暂时没有底部loadingItem。本类可以忽略。
 */
abstract class AutoLoadMore2BindRcvAdapter<DATA:Any, VH: BindViewHolder<DATA, *>>(private val loadingMoreBean:DATA) :
    AutoLoadMoreBindRcvAdapter<DATA, VH>() {
    fun isLoadMoreType(bean:DATA):Boolean = (bean == loadingMoreBean)
    fun isLoadMoreHolder(position: Int): Boolean {
        return if (position in 0 until itemCount) {
            isLoadMoreType(datas[position])
        } else {
            false
        }
    }

    override fun appendDatasOnly(appendList: List<DATA>?) {
        val last = datas.lastOrNull()
        if (appendList.isNullOrEmpty()) {
            //如果传入的新数据
            if (last != null && isLoadMoreType(last)) {
                removeItem(last)
            }
        } else {
            val oldCount = itemCount
            val realDatas = mutableListOf<DATA>()
            realDatas.addAll(appendList)
            val loadingBean = loadingMoreBean
            if (hasMore) {
                realDatas.add(loadingBean)
            }
            if (last != null && isLoadMoreType(last)) {
                removeItems(oldCount - 1, 1)
            }
            addItems(realDatas)
        }
    }

    override fun initDatasOnly(datas: List<DATA>?, isTraditionalForce: Boolean) {
        if (datas.isNullOrEmpty()) {
            //注意，我们为了屏蔽外部使用，已经改写；所以这里调用super
            super.updateDataList(null, false, isTraditionalForce)
        } else {
            val realDatas = mutableListOf<DATA>()
            realDatas.addAll(datas)
            val loadingBean = loadingMoreBean
            if (hasMore) {
                realDatas.add(loadingBean)
            }
            //注意，我们为了屏蔽外部使用，已经改写；所以这里调用super
            super.updateDataList(realDatas, false, isTraditionalForce)
        }
    }
}