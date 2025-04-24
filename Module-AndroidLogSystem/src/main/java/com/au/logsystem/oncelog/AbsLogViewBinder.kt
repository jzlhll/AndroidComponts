package com.au.logsystem.oncelog

import androidx.viewbinding.ViewBinding
import com.au.module_android.utils.asOrNull
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

abstract class AbsLogViewBinder<BINDING: ViewBinding>(binding: BINDING) : BindViewHolder<LogViewNormalBean, BINDING>(binding) {
    companion object {
        const val VIEW_TYPE_WRAP = 101
        const val VIEW_TYPE_NO_WRAP = 102
    }

    private fun getColor(lvl:String) : Int?{
        val adapter = bindingAdapter.asOrNull<LogViewAdapter>() ?: return null

        return when (lvl) {
            "E" -> adapter.errorColor
            "W" -> adapter.warnColor
            else-> adapter.debugColor
        }
    }

    protected fun setText(tv: android.widget.TextView, bean: LogViewNormalBean) {
        val c = getColor(bean.info?.level ?: "D")
        if(c != null) tv.setTextColor(c)
        if (bean.showBits.isShouldShowOriginal()) {
            tv.text = bean.orig
        } else {
            val sb = StringBuilder()
            if (bean.showBits.time) {
                bean.info?.time?.also { sb.append(it) }
            }
            if (bean.showBits.threadProcess) {
                bean.info?.threadProcess?.also {
                    if (sb.isNotEmpty()) {
                        sb.append(" ")
                    }
                    sb.append(it)
                }
            }
            if (bean.showBits.level) {
                bean.info?.level?.also {
                    if (sb.isNotEmpty()) {
                        sb.append(" ")
                    }
                    sb.append(it)
                }
            }
            if (bean.showBits.tag) {
                bean.info?.tag?.also {
                    if (sb.isNotEmpty()) {
                        sb.append(" ")
                    }
                    sb.append(it)
                }
            }
            bean.info?.msg?.also {
                if (sb.isNotEmpty()) {
                    sb.append(" ")
                }
                sb.append(it)
            }
            tv.text = sb
        }
    }
}