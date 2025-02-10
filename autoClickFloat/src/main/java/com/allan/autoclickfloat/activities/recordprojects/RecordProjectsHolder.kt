package com.allan.autoclickfloat.activities.recordprojects

import com.allan.autoclickfloat.databinding.RecordProjectOneItemBinding
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import com.au.module_android.click.onClick
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

/**
 * @author allan
 * @date :2024/4/23 17:52
 * @description:
 */
class RecordProjectsHolder(binding:RecordProjectOneItemBinding, val itemClick:(RecordProjectsItemInfo)->Unit)
        : BindViewHolder<RecordProjectsItemInfo, RecordProjectOneItemBinding>(binding) {
    init {
        binding.root.onClick {
            currentData?.let {
                itemClick(it)
            }
        }
    }

    override fun bindData(bean: RecordProjectsItemInfo) {
        super.bindData(bean)
        if (bean.isSelectMode) {
            binding.checkbox.visible()
        } else {
            binding.checkbox.gone()
        }

        binding.text.text = bean.project.projectName
    }
}