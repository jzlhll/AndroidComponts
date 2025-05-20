package com.allan.androidlearning.transfer.views

import android.annotation.SuppressLint
import com.allan.androidlearning.databinding.HolderMydroidFileitemBinding
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.allan.androidlearning.transfer.getIcon
import com.au.module_android.click.onClick
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import java.io.File

class MyDroidReceiveViewHolder(binding: HolderMydroidFileitemBinding,
                               fullClick:(MergedFileInfo)->Unit,
                               click:(File)->Unit) : BindViewHolder<MergedFileInfo, HolderMydroidFileitemBinding>(binding) {
    init {
        binding.root.onClick {
            val d = currentData ?: return@onClick
            fullClick(d)
        }

        binding.actionBtn.onClick {
            val d = currentData ?: return@onClick
            val file = d.file
            click(file)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun bindData(bean: MergedFileInfo) {
        super.bindData(bean)
        binding.fileNameTv.text = bean.file.name
        binding.fileSizeAndMD5Tv.text = bean.md5 + "   " + bean.fileSizeInfo
        binding.icon.setImageResource(getIcon(bean.file.name))
    }
}