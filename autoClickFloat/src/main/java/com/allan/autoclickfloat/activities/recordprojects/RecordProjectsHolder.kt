package com.allan.autoclickfloat.activities.recordprojects

import com.allan.autoclickfloat.databinding.RecordProjectOneItemBinding
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

/**
 * @author allan
 * @date :2024/4/23 17:52
 * @description:
 */
class RecordProjectsHolder(binding:RecordProjectOneItemBinding) : BindViewHolder<RecordProjectsItemInfo, RecordProjectOneItemBinding>(binding) {
    override fun bindData(bean: RecordProjectsItemInfo) {
        super.bindData(bean)
        if (bean.isSelectMode) {
            binding.checkbox.visible()
        } else {
            binding.checkbox.gone()
        }

        binding.text.text = bean.str
    }
}