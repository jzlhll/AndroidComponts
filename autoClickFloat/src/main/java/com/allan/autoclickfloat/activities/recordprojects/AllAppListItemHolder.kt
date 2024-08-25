package com.allan.autoclickfloat.activities.recordprojects

import com.allan.autoclickfloat.R
import com.allan.autoclickfloat.databinding.AllAppListHolderBinding
import com.allan.nested.recyclerview.viewholder.BindViewHolder

/**
 * @author allan
 * @date :2024/6/5 15:08
 * @description:
 */
class AllAppListItemHolder(binding: AllAppListHolderBinding) : BindViewHolder<AllAppListItemBean, AllAppListHolderBinding>(binding) {
    override fun bindData(bean: AllAppListItemBean) {
        super.bindData(bean)
        binding.text.text = bean.name
        val dr = bean.drawable
        if (dr != null) {
            binding.icon.setImageDrawable(dr)
        } else {
            binding.icon.setImageResource(R.drawable.ic_launcher_foreground)
        }
    }
}