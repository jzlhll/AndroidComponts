package com.au.jobstudy.star

import android.view.ViewGroup
import com.allan.nested.recyclerview.BindRcvAdapter
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.databinding.HolderFriendItemBinding

class StarAdapter : BindRcvAdapter<Star, FriendItemViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendItemViewHolder {
        return FriendItemViewHolder(create(parent))
    }

    override fun onBindViewHolder(holder: FriendItemViewHolder, position: Int) {
        holder.bindData(datas[position])
    }
}

class FriendItemViewHolder(vh:HolderFriendItemBinding) : BindViewHolder<Star, HolderFriendItemBinding>(vh) {
    override fun bindData(bean: Star) {
        super.bindData(bean)

        binding.name.text = bean.name
        binding.starCount.text = "" + bean.starCount
        binding.dingCount.text = "" + bean.dingCount
    }
}