package com.au.jobstudy.completed

import android.view.ViewGroup
import com.allan.nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.allan.nested.recyclerview.DiffCallback
import com.allan.nested.recyclerview.viewholder.BindViewHolder

class CompletedAdapter(private val itemClick:(CompletedBean)->Unit) : AutoLoadMoreBindRcvAdapter<ICompletedBean, BindViewHolder<ICompletedBean, *>>() {
    override fun isSupportDiffer(): Boolean {
        return true
    }

    override fun createDiffer(a: List<ICompletedBean>?, b: List<ICompletedBean>?): DiffCallback<ICompletedBean> {
        return Differ(a, b)
    }

    class Differ(aList:List<ICompletedBean>?, bList:List<ICompletedBean>?) : DiffCallback<ICompletedBean>(aList, bList) {
        override fun compareContent(a: ICompletedBean, b: ICompletedBean): Boolean {
            val aIsCompletedBean = a is CompletedBean
            val bIsCompletedBean = b is CompletedBean

            if (aIsCompletedBean && bIsCompletedBean) {
                if (a == b) {
                    return true
                }
                if ((a as CompletedBean).workEntity.id == (b as CompletedBean).workEntity.id) {
                    return true
                }
                return false
            } else if (!aIsCompletedBean && !bIsCompletedBean) {
                return (a as CompletedDateBean).day == (b as CompletedDateBean).day && a.isWeek == b.isWeek
            } else {
                return false
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder<ICompletedBean, *> {
        return if(viewType == 1) CompletedViewHolder(itemClick, create(parent))
        else CompletedDateViewHolder(create(parent))
    }

    override fun getItemViewType(position: Int): Int {
        return if(datas[position] is CompletedDateBean) 0 else 1
    }
}
