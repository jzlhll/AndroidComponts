package com.au.jobstudy.home

import android.view.ViewGroup
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

/**
 * @author au
 * @date :2023/12/1 16:45
 * @description:
 */
class HomeRcvAdapter : BindRcvAdapter<HomeRcvBean, BindViewHolder<HomeRcvBean, *>>() {
    companion object {
        var click:((HomeRcvItemBean)->Unit)? = null
    }

    var headBinding:HomeRcvHeadViewHolder? = null
    var headBindingCreatedCallback : ((HomeRcvHeadViewHolder)->Unit) ? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder<HomeRcvBean, *> {
        return when (viewType) {
            HomeRcvBean.VIEW_TYPE_MARKUP -> {
                HomeRcvMarkupViewHolder(create(parent))
            }
            HomeRcvBean.VIEW_TYPE_TITLE -> {
                HomeRcvTitleViewHolder(create(parent))
            }
            HomeRcvBean.VIEW_TYPE_ITEM -> {
                HomeRcvItemViewHolder(create(parent))
            }

            HomeRcvBean.VIEW_TYPE_HEAD -> HomeRcvHeadViewHolder(this, create(parent)).also {
                headBinding = it
            }
            else -> throw RuntimeException("no way.")
        }
    }

    override fun onBindViewHolder(holder: BindViewHolder<HomeRcvBean, *>, position: Int) {
        holder.bindData(datas[position])
    }

    override fun getItemViewType(position: Int): Int {
        return datas[position].viewType
    }
}
