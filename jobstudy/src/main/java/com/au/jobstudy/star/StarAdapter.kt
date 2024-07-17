package com.au.jobstudy.star

import android.view.View
import android.view.ViewGroup
import com.allan.nested.recyclerview.BindRcvAdapter
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.MainStarsFragment

class StarAdapter(val f : MainStarsFragment) : BindRcvAdapter<IStarBean, BindViewHolder<IStarBean, *>>() {
    private val itemBeforeClick: ((View, StarItemBean)->Unit) = { v, bean->
        val rect = intArrayOf(0, 0)
        v.getLocationOnScreen(rect)
        f.binding.dingView.startRunning(rect[0], rect[1])
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder<IStarBean, *> {
        if (viewType == VIEW_TYPE_MARKUP) {
            return StarMarkupViewHolder(create(parent))
        }
        if (viewType == VIEW_TYPE_HEAD) {
            return StarHeadViewHolder(create(parent))
        }
        return StarItemViewHolder(create(parent), itemBeforeClick)
    }

    override fun onBindViewHolder(holder: BindViewHolder<IStarBean, *>, position: Int) {
        holder.bindData(datas[position])
    }

    override fun getItemViewType(position: Int): Int {
        val star = datas[position]
        return when (star) {
            is StarHeadBean -> {
                VIEW_TYPE_HEAD
            }
            is StarMarkupBean -> {
                VIEW_TYPE_MARKUP
            }
            else -> {
                VIEW_TYPE_ITEM
            }
        }
    }
}

