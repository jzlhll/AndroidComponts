package com.allan.mydroid.views.receiver

import android.annotation.SuppressLint
import com.allan.mydroid.beans.MergedFileInfo
import com.allan.mydroid.databinding.HolderMydroidReceiverFileitemBinding
import com.allan.mydroid.globals.getIcon
import com.au.module_android.click.onClick
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import java.io.File

class MyDroidReceiveViewHolder(binding: HolderMydroidReceiverFileitemBinding,
                               fullClick:(MergedFileInfo)->Unit,
                               click:(File)->Unit) : BindViewHolder<MergedFileInfo, HolderMydroidReceiverFileitemBinding>(binding) {
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
        val md5Sub = (if(bean.md5.length > 8) bean.md5.substring(0, 8) else bean.md5)
        binding.fileSizeAndMD5Tv.text = md5Sub + "  " + bean.fileSizeInfo
        binding.icon.setImageResource(getIcon(bean.file.name))
    }
}