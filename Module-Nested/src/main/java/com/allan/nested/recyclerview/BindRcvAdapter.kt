package com.allan.nested.recyclerview

import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.module_android.utils.withMainThread

/**
 * author: allan
 * Time: 2022/11/22
 * Desc: 简单的封装，提供diff算法；和一些增删改动的动作；提供基础list。
 *
 *  不支持精细化更新。
 * 如果支持精细化更新，请设定使用AutoLoadMoreBindRcvAdapter。
 *
 * 在[onCreateViewHolder]中使用create()函数来创建viewHolder。
 *
 */
abstract class BindRcvAdapter<DATA:Any, VH: BindViewHolder<DATA, *>> : BaseAdapter<DATA, VH>(), IBindAdapter<DATA> {

    /**
     * 主线程刷新 如果是占位图显示；则需要调用initWithPlacesHolder。替换的时候，不能做差异化更新。
     */
    override fun submitList(
        newList: List<DATA>?,
        isReplaceData: Boolean,
    ) {
        isPlacesHolder = false
        submitTraditional(newList)
    }

    /**
     * 异步刷新
     */
    override suspend fun submitListAsync(
        newList: List<DATA>?,
        isReplaceData: Boolean,
    ) {
        withMainThread {
            submitList(newList, isReplaceData)
        }
    }
}