package com.au.logsystem

import android.view.ViewGroup
import com.au.logsystem.databinding.HolderLogViewNormalBinding
import com.au.module_nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.au.module_nested.recyclerview.DiffCallback
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class LogViewAdapter : AutoLoadMoreBindRcvAdapter<LogViewNormalBean, LogViewBinder>() {
    override fun isSupportDiffer(): Boolean {
        return true
    }

    private class Differ(aList:List<LogViewNormalBean>?, bList:List<LogViewNormalBean>?) : DiffCallback<LogViewNormalBean>(aList, bList) {
        override fun compareContent(a: LogViewNormalBean, b: LogViewNormalBean): Boolean {
            return a.index == b.index
        }
    }

    override fun createDiffer(a: List<LogViewNormalBean>?, b: List<LogViewNormalBean>?): DiffCallback<LogViewNormalBean> {
        return Differ(a, b)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewBinder {
        return LogViewBinder(create(parent))
    }
}

class LogViewBinder(binding: HolderLogViewNormalBinding) : BindViewHolder<LogViewNormalBean, HolderLogViewNormalBinding>(binding) {
    override fun bindData(bean: LogViewNormalBean) {
        super.bindData(bean)
        binding.root.text = bean.string
    }
}