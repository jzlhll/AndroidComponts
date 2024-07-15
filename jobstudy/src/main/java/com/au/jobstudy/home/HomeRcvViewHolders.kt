package com.au.jobstudy.home

import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.R
import com.au.jobstudy.databinding.HolderHomeHeadBinding
import com.au.jobstudy.databinding.HomeCheckItemBinding
import com.au.jobstudy.databinding.HomeCheckItemTitleBinding
import com.au.module_android.Globals
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

class HomeRcvHeadViewHolder(val adapter:HomeRcvAdapter, viewBinding: HolderHomeHeadBinding) : BindViewHolder<HomeRcvBean, HolderHomeHeadBinding>(viewBinding) {
    override fun bindData(bean: HomeRcvBean) {
        super.bindData(bean)
        bean as HomeRcvHeadBean
        binding.mineName.text = bean.userName
        binding.mineScholl.text = bean.scroll
        binding.mineWeeklyText.text = String.format(Globals.app.resources.getString(R.string.this_week_works_list), bean.starCount)
        binding.mineStarText.text = "" + bean.starCount
        binding.mineDingCount.text = "" + bean.dingCount
        adapter.headBindingCreatedCallback?.invoke(this)
    }
}

class HomeRcvTitleViewHolder(viewBinding: HomeCheckItemTitleBinding) : BindViewHolder<HomeRcvBean, HomeCheckItemTitleBinding>(viewBinding) {
    override fun bindData(bean: HomeRcvBean) {
        super.bindData(bean)
        bean as HomeRcvTitleBean
        binding.workTitle.text = bean.title
        if (bean.isFirstTitle) {
            binding.secondGroupSpace.gone()
        } else {
            binding.secondGroupSpace.visible()
        }
    }
}

class HomeRcvItemViewHolder(viewBinding: HomeCheckItemBinding) : BindViewHolder<HomeRcvBean, HomeCheckItemBinding>(viewBinding) {
    init {
        setHolderClick {
            if (it != null) {
                HomeRcvAdapter.click?.invoke(it as HomeRcvItemBean)
            }
        }
    }

    override fun bindData(bean: HomeRcvBean) {
        super.bindData(bean)
        bean as HomeRcvItemBean
        binding.descText.text = bean.oneWork.desc
        binding.subjectText.text = bean.oneWork.subject
//        binding.subjectColor.setBackgroundColor(binding.root.resources.getColor(bean.colorId))
        binding.subjectColor.background = ViewBackgroundBuilder()
            .setBackground(binding.root.resources.getColor(bean.colorId))
            .setCornerRadius(3f.dp)
            .build()
    }
}