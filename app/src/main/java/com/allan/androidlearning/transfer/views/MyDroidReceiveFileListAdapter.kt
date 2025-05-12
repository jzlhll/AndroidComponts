package com.allan.androidlearning.transfer.views

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.au.module_nested.recyclerview.BindRcvAdapter

class MyDroidReceiveFileListAdapter(val f:Fragment) : BindRcvAdapter<MergedFileInfo, MyDroidReceiveViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDroidReceiveViewHolder {
        return MyDroidReceiveViewHolder(create(parent))
    }
}