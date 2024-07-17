package com.au.jobstudy.completed

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import com.allan.nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.allan.nested.recyclerview.DiffCallback
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.databinding.HolderCompletedDateItemBinding
import com.au.jobstudy.databinding.HolderCompletedItemBinding
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.dp

class CompletedAdapter : AutoLoadMoreBindRcvAdapter<ICompletedBean, BindViewHolder<ICompletedBean, *>>() {
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
        return if(viewType == 1) CompletedViewHolder(create(parent))
        else CompletedDateViewHolder(create(parent))
    }

    override fun onBindViewHolder(holder: BindViewHolder<ICompletedBean, *>, position: Int) {
        super.onBindViewHolder(holder, position)
        holder.bindData(datas[position])
    }

    override fun getItemViewType(position: Int): Int {
        return if(datas[position] is CompletedDateBean) 0 else 1
    }
}

class CompletedViewHolder(vh:HolderCompletedItemBinding) : BindViewHolder<ICompletedBean, HolderCompletedItemBinding>(vh) {
    override fun bindData(bean: ICompletedBean) {
        super.bindData(bean)
        bean as CompletedBean
        binding.sucImage.visibility = if (bean.completedEntity != null) View.VISIBLE else View.GONE
        binding.descText.text = bean.workEntity.desc
        binding.subjectText.text = bean.workEntity.subject
        binding.subjectColor.background = ViewBackgroundBuilder()
            .setBackground(Color.parseColor(bean.workEntity.colorStr))
            .setCornerRadius(3f.dp)
            .build()
        binding.weekText.visibility = if(bean.workEntity.weekJob) View.VISIBLE else View.GONE
        binding.host.alpha = if (bean.completedEntity == null) 0.5f else 1f
    }
}

class CompletedDateViewHolder(vh: HolderCompletedDateItemBinding) : BindViewHolder<ICompletedBean, HolderCompletedDateItemBinding>(vh) {
    override fun bindData(bean: ICompletedBean) {
        super.bindData(bean)
        bean as CompletedDateBean
        binding.dateText.text = (if(bean.isWeek) "Week: " else "") + bean.day
    }
}