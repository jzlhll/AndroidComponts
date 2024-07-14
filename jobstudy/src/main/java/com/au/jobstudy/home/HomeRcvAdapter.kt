package com.au.jobstudy.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.allan.nested.layout.SimpleItemsLayout
import com.allan.nested.recyclerview.BindRcvAdapter
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.databinding.HolderHomeHeadBinding
import com.au.jobstudy.databinding.HomeCheckItemBinding
import com.au.jobstudy.databinding.HomeCheckItemTitleBinding
import com.au.jobstudy.databinding.HomeStarOnlyOneBigBinding
import com.au.jobstudy.databinding.HomeStarThreeStarsBinding
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

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
            1 -> {
                HomeRcvTitleViewHolder(create(parent))
            }
            2 -> {
                HomeRcvItemViewHolder(create(parent))
            }

            3 -> HomeRcvHeadViewHolder(this, create(parent)).also {
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

class HomeRcvHeadViewHolder(val adapter:HomeRcvAdapter, viewBinding: HolderHomeHeadBinding) : BindViewHolder<HomeRcvBean, HolderHomeHeadBinding>(viewBinding) {
    override fun bindData(bean: HomeRcvBean) {
        super.bindData(bean)
        bean as HomeRcvHeadBean
        binding.mineName.text = bean.userName
        binding.mineScholl.text = bean.scroll
//        binding.thisWeekList.itemInflateCreator = object : ((LayoutInflater, SimpleItemsLayout, Boolean, Any)-> ViewBinding) {
//            override fun invoke(layoutInflate: LayoutInflater, me: SimpleItemsLayout, attachedToParent: Boolean, data: Any): ViewBinding {
//                return when (val uiData = data as ThisWeekUiData) {
//                    is ThisWeekUiData.ThisWeekLayoutData -> {
//                        HomeStarOnlyOneBigBinding.inflate(layoutInflate, me, attachedToParent).also {
//                            it.numbersTv.text = "${uiData.num}"
//                        }
//                    }
//
//                    is ThisWeekUiData.ThisWeekEachLayoutData -> {
//                        HomeStarThreeStarsBinding.inflate(layoutInflate, me, attachedToParent).also { vb->
//                            var count = 0
//                            uiData.eachStars.forEach {
//                                count++
//                                when (count) {
//                                    1 -> {
//                                        vb.name1.text = it.first.name
//                                        vb.value1.text = "${it.second}"
//                                        vb.name1.visible()
//                                        vb.value1.visible()
//                                        vb.pic1.visible()
//                                    }
//                                    2 -> {
//                                        vb.name2.text = it.first.name
//                                        vb.value2.text = "${it.second}"
//                                        vb.name2.visible()
//                                        vb.value2.visible()
//                                        vb.pic2.visible()
//                                    }
//                                    3 -> {
//                                        vb.name3.text = it.first.name
//                                        vb.value3.text = "${it.second}"
//                                        vb.name3.visible()
//                                        vb.value3.visible()
//                                        vb.pic3.visible()
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }

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
