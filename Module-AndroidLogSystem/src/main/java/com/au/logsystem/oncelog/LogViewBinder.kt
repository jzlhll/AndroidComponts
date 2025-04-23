package com.au.logsystem.oncelog

import com.au.logsystem.databinding.HolderLogViewNormalBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utils.unsafeLazy
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class LogViewBinder(binding: HolderLogViewNormalBinding) : BindViewHolder<LogViewNormalBean, HolderLogViewNormalBinding>(binding) {
    companion object {
        private val errorColor by unsafeLazy {
            Globals.getColor(com.au.logsystem.R.color.color_log_error)
        }
        private val warnColor by unsafeLazy {
            Globals.getColor(com.au.logsystem.R.color.color_log_warn)
        }
        private val debugColor by unsafeLazy {
            Globals.getColor(com.au.module_androidcolor.R.color.color_text_normal)
        }

        private fun getColor(lvl:String) : Int{
            return when (lvl) {
                "E"-> errorColor
                "W"-> warnColor
                else-> debugColor
            }
        }
    }

    init {
        binding.root.onClick {
            val cur = currentData ?: return@onClick
            cur.isUiWrapText = !cur.isUiWrapText
        }
    }

    override fun bindData(bean: LogViewNormalBean) {
        super.bindData(bean)
        val c = getColor(bean.info?.level ?: "D")
        binding.tv.setTextColor(c)
        binding.tv.text = bean.orig
    }
}