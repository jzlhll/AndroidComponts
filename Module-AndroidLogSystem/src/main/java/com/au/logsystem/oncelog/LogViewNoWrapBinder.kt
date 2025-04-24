package com.au.logsystem.oncelog

import com.au.logsystem.databinding.HolderLogViewNoWrapBinding

class LogViewNoWrapBinder(binding: HolderLogViewNoWrapBinding) : AbsLogViewBinder<HolderLogViewNoWrapBinding>(binding) {
    override fun bindData(bean: LogViewNormalBean) {
        super.bindData(bean)
        setText(binding.tv, bean)
    }
}