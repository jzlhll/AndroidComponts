package com.au.jobstudy.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.allan.nested.layout.SimpleItemsLayout
import com.allan.nested.recyclerview.BindRcvAdapter
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.jobstudy.databinding.HolderHomeHeadBinding
import com.au.jobstudy.databinding.HomeCheckItemBinding
import com.au.jobstudy.databinding.HomeCheckItemTitleBinding
import com.au.jobstudy.databinding.HomeStarOnlyOneBigBinding
import com.au.jobstudy.databinding.HomeStarThreeStarsBinding
import com.au.module_android.utils.ViewBackgroundBuilder
import com.au.module_android.utils.dp
import com.au.module_android.utils.gone
import com.au.module_android.utils.visible

/**
 * @author au
 * @date :2023/12/1 16:45
 * @description:
 */
class HomeRcvAdapter : BindRcvAdapter<HomeRcvBean, BindViewHolder<HomeRcvBean, *>>() {
    companion object {
        var click:((HomeRcvItemBean)->Unit)? = null
    }

    var headBinding:HomeRcvHeadViewHolder? = null
    var headBindingCreatedCallback : ((HomeRcvHeadViewHolder)->Unit) ? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindViewHolder<HomeRcvBean, *> {
        return when (viewType) {
            1 -> {
                HomeRcvTitleViewHolder(create(parent))
            }
            2 -> {
                HomeRcvItemViewHolder(create(parent))
            }

            3 -> HomeRcvHeadViewHolder(this, create(parent)).also {
                headBinding = it
            }
            else -> throw RuntimeException("no way.")
        }
    }

    override fun onBindViewHolder(holder: BindViewHolder<HomeRcvBean, *>, position: Int) {
        holder.bindData(datas[position])
    }

    override fun getItemViewType(position: Int): Int {
        return datas[position].viewType
    }
}
