package com.allan.androidlearning.transfer.views.receiver

import android.view.ViewGroup
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.au.module_nested.recyclerview.BindRcvAdapter
import java.io.File

class MyDroidReceiveFileListAdapter(val fullClick:(MergedFileInfo)->Unit, val click:(File)->Unit) : BindRcvAdapter<MergedFileInfo, MyDroidReceiveViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDroidReceiveViewHolder {
        return MyDroidReceiveViewHolder(create(parent), fullClick, click)
    }
}