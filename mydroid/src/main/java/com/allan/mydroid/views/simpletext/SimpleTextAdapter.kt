package com.allan.mydroid.views.simpletext

import android.view.ViewGroup
import com.allan.mydroid.databinding.HolderMydroidSimpletextBinding
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class SimpleTextAdapter : BindRcvAdapter<SimpleTextUiItem, SimpleTextViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SimpleTextViewHolder {
        return SimpleTextViewHolder(create(parent))
    }
}

class SimpleTextViewHolder(binding: HolderMydroidSimpletextBinding)
        : BindViewHolder<SimpleTextUiItem, HolderMydroidSimpletextBinding>(binding) {
    override fun bindData(bean: SimpleTextUiItem) {
        super.bindData(bean)
        if (!bean.leftText.isNullOrEmpty()) {
            binding.leftTv.text = bean.leftText
            binding.leftTvHost.visible()
        } else {
            binding.leftTvHost.gone()
        }

        if (!bean.rightText.isNullOrEmpty()) {
            binding.rightTv.text = bean.rightText
            binding.rightTvHost.visible()
        } else {
            binding.rightTvHost.gone()
        }
    }
}