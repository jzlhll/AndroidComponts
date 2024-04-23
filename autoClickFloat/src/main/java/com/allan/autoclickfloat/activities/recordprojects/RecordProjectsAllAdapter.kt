package com.allan.autoclickfloat.activities.recordprojects

import android.view.ViewGroup
import com.allan.nested.recyclerview.BindRcvAdapter

/**
 * @author allan
 * @date :2024/4/23 17:49
 * @description:
 */
class RecordProjectsAllAdapter : BindRcvAdapter<RecordProjectsItemInfo, RecordProjectsHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordProjectsHolder {
        return RecordProjectsHolder(create((parent)))
    }

    override fun onBindViewHolder(holder: RecordProjectsHolder, position: Int) {
        holder.bindData(datas[position])
    }

}