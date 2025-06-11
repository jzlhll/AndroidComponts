package com.allan.mydroid.views.textchat

import android.view.ViewGroup
import com.allan.mydroid.views.textchat.uibean.AbsItem
import com.allan.mydroid.views.textchat.uibean.AbsItem.VIEW_TYPE_ME
import com.allan.mydroid.views.textchat.uibean.AbsItem.VIEW_TYPE_OTHER
import com.allan.mydroid.views.textchat.uibean.AbsItem.VIEW_TYPE_STATUS
import com.au.module_android.utils.NoWayException
import com.au.module_nested.recyclerview.BindRcvAdapter
import com.au.module_nested.recyclerview.viewholder.BindViewHolder

class TextChatRcvAdapter : BindRcvAdapter<AbsItem, BindViewHolder<AbsItem, *>>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): BindViewHolder<AbsItem, *> {
        return when(viewType) {
            VIEW_TYPE_STATUS -> {
                TextChatRcvHolderStatus(create(parent))
            }
            VIEW_TYPE_ME -> {
                TextChatRcvHolderMe(create(parent))
            }
            VIEW_TYPE_OTHER -> {
                TextChatRcvHolderOther(create(parent))
            }
            else -> {
                throw NoWayException()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return datas[position].viewType
    }
}