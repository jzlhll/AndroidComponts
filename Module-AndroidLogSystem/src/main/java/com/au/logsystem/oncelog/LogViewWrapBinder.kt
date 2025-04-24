package com.au.logsystem.oncelog

import com.au.logsystem.databinding.HolderLogViewWrapBinding

class LogViewWrapBinder(binding: HolderLogViewWrapBinding) : AbsLogViewBinder<HolderLogViewWrapBinding>(binding) {
    override fun bindData(bean: LogViewNormalBean) {
        super.bindData(bean)
        setText(binding.tv, bean)
    }
}