package com.allan.mydroid.views.send

import android.view.ViewGroup
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.databinding.HolderMydroidSendlistItemBinding
import com.allan.mydroid.globals.getIcon
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class SendListAdapter : BindRcvAdapter<UriRealInfoEx, SendListAdapter.ShareReceiverHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShareReceiverHolder {
        return ShareReceiverHolder(create(parent))
    }

    class ShareReceiverHolder(binding: HolderMydroidSendlistItemBinding) : BindViewHolder<UriRealInfoEx, HolderMydroidSendlistItemBinding>(binding) {
        private val targetAlpha = 0.5f

        init {
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                currentData?.isChecked = isChecked
                binding.root.alpha = if (isChecked) 1f else targetAlpha
            }
        }

        override fun bindData(bean: UriRealInfoEx) {
            super.bindData(bean)
            val goodName = bean.goodName()
            binding.icon.setImageResource(getIcon(bean.goodName()))
            binding.fileNameTv.text = goodName ?: bean.uri.toString()
            binding.fileSizeAndMD5Tv.text = bean.fileSizeStr
            binding.checkBox.isChecked = bean.isChecked

            binding.root.alpha = if (bean.isChecked) 1f else targetAlpha
        }
    }
}