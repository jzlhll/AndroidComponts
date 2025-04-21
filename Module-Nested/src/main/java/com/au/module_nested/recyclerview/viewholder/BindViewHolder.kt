package com.au.module_nested.recyclerview.viewholder

import androidx.annotation.CallSuper
import androidx.annotation.EmptySuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.au.module_android.click.onClick

/**
 * author: allan
 * Time: 2022/11/22
 * Desc: DATA表示数据类型；BINDING代表绑定的Xml自动转换的java类ViewBinding子类。
 * 增加1个beanTag
 */
abstract class BindViewHolder<DATA:Any, BINDING: ViewBinding>(val binding: BINDING) : RecyclerView.ViewHolder(binding.root) {
    private var current:DATA? = null

    private var mItemClick1:((DATA?, index:Int)->Unit)? = null
    private var mItemClick2:((DATA?)->Unit)? = null

    fun setHolderClick(click:(DATA?)->Unit) {
        mItemClick1 = null
        mItemClick2 = click
        binding.root.onClick {
            mItemClick2?.invoke(current)
        }
    }

    fun setHolderClickWithAdapterPosition(click:(DATA?, adapterPosition:Int)->Unit) {
        mItemClick1 = click
        mItemClick2 = null
        binding.root.onClick {
            mItemClick1?.invoke(current, absoluteAdapterPosition)
        }
    }

    /**
     * 获取bindData时候的tag。
     */
    val currentData:DATA? get() = current

    @CallSuper
    open fun bindData(bean: DATA) {
        current = bean
    }

    @CallSuper
    open fun payloadsRefresh(bean:DATA, payloads:MutableList<Any>) {
        current = bean
    }

    @EmptySuper
    open fun onDetached() {}
    @EmptySuper
    open fun onAttached() {}
}
