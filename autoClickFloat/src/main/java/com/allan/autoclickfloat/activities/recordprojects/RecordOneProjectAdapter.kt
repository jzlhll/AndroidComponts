package com.allan.autoclickfloat.activities.recordprojects

import android.view.ViewGroup
import com.allan.autoclickfloat.database.Step
import com.allan.autoclickfloat.databinding.RecordProjectOneItemBinding
import com.allan.nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.allan.nested.recyclerview.DiffCallback
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.module_android.utils.HtmlPart
import com.au.module_android.utils.useSimpleHtmlText

data class StepWrap(val isSelected:Boolean, val step:Step)

class RecordOneProjectAdapter : AutoLoadMoreBindRcvAdapter<StepWrap, RecordOneProjectViewHolder>() {
    override fun isSupportDiffer() = true

    override fun createDiffer(a: List<StepWrap>?, b: List<StepWrap>?): DiffCallback<StepWrap>? {
        return Differ(a, b)
    }

    private class Differ(a: List<StepWrap>?, b: List<StepWrap>?) : DiffCallback<StepWrap>(a, b) {
        override fun compareContent(a: StepWrap, b: StepWrap): Boolean {
            return a == b //那么就要求数据，每次来都拷贝更新StepWrap对象
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordOneProjectViewHolder {
        return RecordOneProjectViewHolder(create(parent))
    }

    override fun getItemViewType(position: Int) = 0
}

class RecordOneProjectViewHolder(binding: RecordProjectOneItemBinding) : BindViewHolder<StepWrap, RecordProjectOneItemBinding>(binding) {
    override fun bindData(bean: StepWrap) {
        super.bindData(bean)
        binding.checkbox.isSelected = bean.isSelected
        binding.text.text = String.format("%02d步: 坐标（%s, %s）", bean.step.stepIndex, bean.step.locX, bean.step.locY)
    }
}