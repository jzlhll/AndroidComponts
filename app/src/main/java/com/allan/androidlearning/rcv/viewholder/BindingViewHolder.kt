package com.allan.androidlearning.rcv.viewholder

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

/**
 * Desc: DATA表示数据类型；BINDING代表绑定的Xml自动转换的java类ViewBinding子类。
 */
abstract class BindingViewHolder<DATA:Any, BINDING: ViewBinding>(val binding: BINDING) :
    RecyclerView.ViewHolder(binding.root) {

    /**
     * 一个item的一个view，从adapter里面公用1个click。
     * 开发建议：所有的recyclerView的items的点击事件都公用同一个click。
     */
    fun setSharedClick(v: View, click: View.OnClickListener) {
        v.setOnClickListener(click)
    }

    /**
     * 当adapter被调用了onBindView的时候，选择好了我们的VH，然后调用bindData
     */
    open fun bindData(bean:DATA) {}
}