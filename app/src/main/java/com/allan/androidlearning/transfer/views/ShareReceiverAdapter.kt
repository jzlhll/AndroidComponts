package com.allan.androidlearning.transfer.views

import android.view.ViewGroup
import com.allan.androidlearning.databinding.HolderShareReceiveItemBinding
import com.allan.androidlearning.transfer.benas.UriRealInfoEx
import com.allan.androidlearning.transfer.getIcon
import com.au.module_android.click.onClick
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class ShareReceiverAdapter : BindRcvAdapter<UriRealInfoEx, ShareReceiverAdapter.ShareReceiverHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareReceiverHolder {
        return ShareReceiverHolder(create(parent))
    }

    class ShareReceiverHolder(binding: HolderShareReceiveItemBinding) : BindViewHolder<UriRealInfoEx, HolderShareReceiveItemBinding>(binding) {
        init {
            binding.root.onClick {
                binding.checkBox.isChecked = !binding.checkBox.isChecked
            }
        }

        override fun bindData(bean: UriRealInfoEx) {
            super.bindData(bean)
            val goodName = bean.goodName()
            binding.icon.setImageResource(getIcon(bean.goodName()))
            binding.fileNameTv.text = goodName ?: bean.uri.toString()
            binding.fileSizeAndMD5Tv.text = bean.fileSizeStr
            binding.checkBox.isChecked = bean.isChecked
        }
    }
}
