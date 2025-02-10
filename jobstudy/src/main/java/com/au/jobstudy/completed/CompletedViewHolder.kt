package com.au.jobstudy.completed

import android.graphics.Color
import android.view.View
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.databinding.HolderCompletedDateItemBinding
import com.au.jobstudy.databinding.HolderCompletedItemBinding
import com.au.jobstudy.utils.WeekDateUtil
import com.au.module_android.click.onClick
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.dp

class CompletedViewHolder(itemClick:(CompletedBean)->Unit
                          , vh: HolderCompletedItemBinding) : BindViewHolder<ICompletedBean, HolderCompletedItemBinding>(vh) {
    init {
        vh.root.onClick {
            if (currentData is CompletedBean) {
                itemClick(currentData as CompletedBean)
            }
        }
    }

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
//        binding.weekText.visibility = if(bean.workEntity.weekJob) View.VISIBLE else View.GONE
    }
}

class CompletedDateViewHolder(vh: HolderCompletedDateItemBinding) : BindViewHolder<ICompletedBean, HolderCompletedDateItemBinding>(vh) {
    override fun bindData(bean: ICompletedBean) {
        super.bindData(bean)
        bean as CompletedDateBean
        if(bean.isWeek) {
            binding.dateText.text = "Week: " + bean.day
        } else {
            var str = WeekDateUtil.checkDateIsTodayOrYesterday(bean.day)
            if (str == null) {
                str = "" + bean.day + " (${WeekDateUtil.getDayOfWeek(bean.day)})"
            }
            binding.dateText.text = "Day: " + str
        }
    }
}