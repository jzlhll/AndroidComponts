package com.allan.autoclickfloat.activities.recordprojects

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.au.module_nested.recyclerview.AutoLoadMoreBindRcvAdapter

/**
 * @author allan
 * @date :2024/6/5 15:08
 * @description:
 */
class AllAppListAdapter : AutoLoadMoreBindRcvAdapter<AllAppListItemBean, AllAppListItemHolder>() {
    override fun isSupportDiffer(): Boolean {
        return false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAppListItemHolder {
        return AllAppListItemHolder(create(parent))
    }

    fun bind(rcv:RecyclerView) {
        rcv.adapter = this
        rcv.layoutManager = StaggeredGridLayoutManager(4, VERTICAL)
    }
}