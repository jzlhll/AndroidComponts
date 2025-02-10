package com.au.jobstudy.home

import android.graphics.Color
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.R
import com.au.jobstudy.databinding.HolderHomeHeadBinding
import com.au.jobstudy.databinding.HomeCheckItemBinding
import com.au.jobstudy.databinding.HomeCheckItemTitleBinding
import com.au.jobstudy.databinding.HomeMarkupBinding
import com.au.module_android.Globals
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

class HomeRcvHeadViewHolder(val adapter:HomeRcvAdapter, viewBinding: HolderHomeHeadBinding) : BindViewHolder<HomeRcvBean, HolderHomeHeadBinding>(viewBinding) {
    override fun bindData(bean: HomeRcvBean) {
        super.bindData(bean)
        bean as HomeRcvHeadBean
        binding.mineScholl.text = bean.scroll
        adapter.headBindingCreatedCallback?.invoke(this)
    }

    fun update(starNum:Int, dingNum:Int) {
        binding.mineWeeklyText.text = String.format(Globals.app.resources.getString(R.string.this_week_works_list), starNum)
        binding.mineStarText.text = "" + starNum
        binding.mineDingCount.text = "" + dingNum
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
        when (bean.colorIndex) {
            3 -> {
                binding.host.setBackgroundResource(R.drawable.corner_title_bar3)
            }
            2 -> {
                binding.host.setBackgroundResource(R.drawable.corner_title_bar2)
            }
            else -> {
                binding.host.setBackgroundResource(R.drawable.corner_title_bar)
            }
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
            .setBackground(Color.parseColor(bean.oneWork.colorStr))
            .setCornerRadius(3f.dp)
            .build()
    }
}

class HomeRcvMarkupViewHolder(viewBinding:HomeMarkupBinding) : BindViewHolder<HomeRcvBean, HomeMarkupBinding>(viewBinding)