package com.allan.mydroid.views.receiver

import android.view.ViewGroup
import com.allan.mydroid.beans.MergedFileInfo
import com.au.module_nested.recyclerview.BindRcvAdapter
import java.io.File

class MyDroidReceiveFileListAdapter(val fullClick:(MergedFileInfo)->Unit, val click:(File)->Unit)
    : BindRcvAdapter<MergedFileInfo, MyDroidReceiveViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDroidReceiveViewHolder {
        return MyDroidReceiveViewHolder(create(parent), fullClick, click)
    }
}