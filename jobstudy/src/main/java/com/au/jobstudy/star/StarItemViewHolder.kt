package com.au.jobstudy.star

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.check.CheckConsts
import com.au.jobstudy.databinding.HolderStarHeadBinding
import com.au.jobstudy.databinding.HolderStarItemBinding
import com.au.jobstudy.databinding.HomeMarkupBinding
import com.au.module_android.click.onClick
import com.au.module_android.utils.asOrNull
import com.au.module_android.utils.toHtml
import com.au.module_android.utils.unsafeLazy

const val VIEW_TYPE_MARKUP = 0
const val VIEW_TYPE_ITEM = 1
const val VIEW_TYPE_HEAD = 2

private val redColorList by unsafeLazy {
    ColorStateList.valueOf(Color.parseColor("#FF9000"))
}
private val grayColorList by unsafeLazy {
    ColorStateList.valueOf(Color.parseColor("#999999"))
}

class StarItemViewHolder(vh: HolderStarItemBinding, itemBeforeClick:((View, StarItemBean)->Unit)) : BindViewHolder<IStarBean, HolderStarItemBinding>(vh) {

    init {
        vh.dingClick.onClick { v->
            currentData.asOrNull<StarItemBean>()?.let {
                itemBeforeClick(v, it)
                if (it.isDing != true) {
                    StarConsts.updateNameDing(it.name, CheckConsts.currentDay())
                    it.dingNum += 1
                    it.isDing = true
                    binding.dingCount.text = "" + it.dingNum
                    binding.dingClick.imageTintList = redColorList
                }
            }
        }
    }

    override fun bindData(bean: IStarBean) {
        super.bindData(bean)
        bean as StarItemBean
        binding.name.text = bean.name
        binding.starCount.text = "" + bean.starNum
        binding.dingCount.text = "" + bean.dingNum
        binding.dingClick.imageTintList = if(bean.isDing == true) redColorList else grayColorList
    }
}

class StarHeadViewHolder(viewBinding: HolderStarHeadBinding) : BindViewHolder<IStarBean, HolderStarHeadBinding>(viewBinding) {
    override fun bindData(bean: IStarBean) {
        super.bindData(bean)
        bean as StarHeadBean
        binding.info.text = toHtml(bean.html)
    }
}

class StarMarkupViewHolder(viewBinding: HomeMarkupBinding) : BindViewHolder<IStarBean, HomeMarkupBinding>(viewBinding)