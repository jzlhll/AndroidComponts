package com.au.jobstudy.home

import android.view.ViewGroup
import com.allan.nested.recyclerview.BindRcvAdapter
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.databinding.HomeCheckItemBinding
import com.au.jobstudy.databinding.HomeCheckItemTitleBinding
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

/**
 * @author allan
 * @date :2023/12/1 16:45
 * @description:
 */
class HomeRcvAdapter : BindRcvAdapter<HomeRcvBean, BindViewHolder<HomeRcvBean, *>>() {
    companion object {
        var click:((HomeRcvItemBean)->Unit)? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder<HomeRcvBean, *> {
        return when (viewType) {
            1 -> {
                HomeRcvTitleViewHolder(create(parent))
            }
            2 -> {
                HomeRcvItemViewHolder(create(parent))
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
        binding.descText.text = bean.dataItem.desc
        binding.subjectText.text = bean.dataItem.subject
//        binding.subjectColor.setBackgroundColor(binding.root.resources.getColor(bean.colorId))
        binding.subjectColor.background = ViewBackgroundBuilder()
            .setBackground(binding.root.resources.getColor(bean.colorId))
            .setCornerRadius(3f.dp)
            .build()
    }
}
