package com.allan.androidlearning.transfer

import android.view.ViewGroup
import com.allan.androidlearning.databinding.HolderMydroidFileitemBinding
import com.au.module_android.Globals
import com.au.module_android.click.onClick
import com.au.module_android.utils.MediaHelper
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class MyDroidTransferFileListAdapter : BindRcvAdapter<MergedFileInfo, MyDroidTransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDroidTransferViewHolder {
        return MyDroidTransferViewHolder(create(parent))
    }
}

class MyDroidTransferViewHolder(binding: HolderMydroidFileitemBinding) : BindViewHolder<MergedFileInfo, HolderMydroidFileitemBinding>(binding) {
    init {
        binding.downloadBtn.onClick {

        }

        binding.sharesBtn.onClick {
            currentData ?: return@onClick
            MediaHelper().shareFile(Globals.app, currentData!!.file)
        }
    }

    override fun bindData(bean: MergedFileInfo) {
        super.bindData(bean)
        binding.fileNameTv.text = bean.file.name
        binding.fileSizeTv.text = bean.fileSizeInfo
        binding.md5Tv.text = bean.md5
    }
}