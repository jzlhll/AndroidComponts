package com.allan.nested.recyclerview.viewholder

import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.au.module_android.click.onClick

/**
 * author: allan.jiang
 * Time: 2022/11/22
 * Desc: DATA表示数据类型；BINDING代表绑定的Xml自动转换的java类ViewBinding子类。
 * 增加1个beanTag
 */
abstract class BindViewHolder<DATA:Any, BINDING: ViewBinding>(val binding: BINDING, itemClick:((DATA?, index:Int)->Unit)? = null) : RecyclerView.ViewHolder(binding.root) {
    private var current:DATA? = null

    init {
        if (itemClick != null) {
            binding.root.onClick {
                itemClick.invoke(current, absoluteAdapterPosition)
            }
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
}
