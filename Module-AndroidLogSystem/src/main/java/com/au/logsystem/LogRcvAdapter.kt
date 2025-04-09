package com.au.logsystem

import android.graphics.drawable.Drawable
import android.view.ViewGroup
import androidx.annotation.Keep
import com.au.logsystem.databinding.FragmentLogSystemHolderDayBinding
import com.au.logsystem.databinding.FragmentLogSystemHolderNormalBinding
import com.au.module_android.Globals
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.unsafeLazy
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import java.io.File

const val TYPE_HEAD = 1
const val TYPE_NORMAL = 0

@Keep
data class LogBean(val type:Int, val info:String, val secondInfo:String, val file: File? = null,
                   var isSelectedMode: Boolean = false, var isSelected: Boolean = false)

fun generateHead(head:String) :  LogBean {
    return LogBean(TYPE_HEAD, head, "")
}

fun generateNormal(info:String, secondInfo: String, file: File) :  LogBean {
    return LogBean(TYPE_NORMAL, info, secondInfo, file)
}

class LogRcvAdapter : BindRcvAdapter<LogBean, BindViewHolder<LogBean, *>>() {
    private val normalBackground by unsafeLazy {
        ViewBackgroundBuilder().setBackground(Globals.getColor(com.au.module_androidcolor.R.color.color_normal_block))
            .setCornerRadius(16f).build()
    }

    private val selectedBackground by unsafeLazy {
        ViewBackgroundBuilder()
            .setBackground(Globals.getColor(com.au.module_androidcolor.R.color.colorPrimary_press))
            .setBackgroundAlpha((0.8f * 255).toInt())
            .setCornerRadius(16f).build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder<LogBean, *> {
        if (viewType == TYPE_HEAD) {
            return LogRcvHeadHolder(create(parent))
        }
        return LogRcvHolder(normalBackground, selectedBackground, create(parent))
    }

    override fun getItemViewType(position: Int): Int {
        return datas[position].type
    }
}

class LogRcvHolder(private val normalBackground: Drawable?,
                   private val selectedBackground: Drawable?,
                   binding: FragmentLogSystemHolderNormalBinding)
    : BindViewHolder<LogBean, FragmentLogSystemHolderNormalBinding>(binding) {
        init {
            binding.root.setOnClickListener {
                if (currentData?.isSelectedMode == true) {
                    currentData?.let {
                        val newState = !it.isSelected
                        it.isSelected = newState
                        binding.bg.background = if (newState) selectedBackground else normalBackground
                    }
                }
            }
        }

    override fun bindData(bean: LogBean) {
        super.bindData(bean)
        binding.nameTv.text = bean.info
        binding.sizeTv.text = bean.secondInfo
        binding.bg.background = if (bean.isSelected) selectedBackground else normalBackground
    }
}

class LogRcvHeadHolder(binding: FragmentLogSystemHolderDayBinding)
    : BindViewHolder<LogBean, FragmentLogSystemHolderDayBinding>(binding) {
    override fun bindData(bean: LogBean) {
        super.bindData(bean)
        binding.title.text = bean.info
    }
}