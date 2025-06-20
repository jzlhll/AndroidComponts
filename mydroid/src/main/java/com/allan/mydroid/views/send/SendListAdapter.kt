package com.allan.mydroid.views.send

import android.view.ViewGroup
import com.allan.mydroid.beansinner.UriRealInfoEx
import com.allan.mydroid.databinding.HolderMydroidSendlistItemBinding
import com.allan.mydroid.databinding.HolderMydroidSendlistItemNoBinding
import com.allan.mydroid.globals.getIcon
import com.au.module_android.click.onClick
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class SendListAdapter(val noButtons:Boolean,
                      val itemClick: (UriRealInfoEx?, mode:String) -> Unit)
        : BindRcvAdapter<UriRealInfoEx, BindViewHolder<UriRealInfoEx, *>>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder<UriRealInfoEx, *> {
        if (noButtons) {
            return ShareReceiverNoButtonsHolder(create(parent), itemClick)
        }
        return ShareReceiverHolder(create(parent), itemClick)
    }

    class ShareReceiverHolder(binding: HolderMydroidSendlistItemBinding, val itemClick: (UriRealInfoEx?, String) -> Unit)
            : BindViewHolder<UriRealInfoEx, HolderMydroidSendlistItemBinding>(binding) {
        private val targetAlpha = 0.5f

        init {
            binding.checkBox.setOnCheckedChangeListener { _, isChecked ->
                currentData?.isChecked = isChecked
                binding.root.alpha = if (isChecked) 1f else targetAlpha
            }

            binding.root.onClick {
                itemClick(currentData, "")
            }

            binding.deleteBtn.onClick {
                itemClick(currentData, "delete")
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

    class ShareReceiverNoButtonsHolder(binding: HolderMydroidSendlistItemNoBinding, val itemClick: (UriRealInfoEx?, String) -> Unit)
        : BindViewHolder<UriRealInfoEx, HolderMydroidSendlistItemNoBinding>(binding) {
        private val targetAlpha = 0.5f

        init {
            binding.root.onClick {
                itemClick(currentData, "")
            }
        }

        override fun bindData(bean: UriRealInfoEx) {
            super.bindData(bean)
            val goodName = bean.goodName()
            binding.icon.setImageResource(getIcon(bean.goodName()))
            binding.fileNameTv.text = goodName ?: bean.uri.toString()
            binding.fileSizeAndMD5Tv.text = bean.fileSizeStr

            binding.root.alpha = if (bean.isChecked) 1f else targetAlpha
        }
    }
}