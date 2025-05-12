package com.allan.androidlearning.transfer.views

import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.allan.androidlearning.transfer.benas.MergedFileInfo
import com.au.module_nested.recyclerview.BindRcvAdapter

class MyDroidTransferFileListAdapter(val f:Fragment) : BindRcvAdapter<MergedFileInfo, MyDroidTransferViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyDroidTransferViewHolder {
        return MyDroidTransferViewHolder(create(parent))
    }
}