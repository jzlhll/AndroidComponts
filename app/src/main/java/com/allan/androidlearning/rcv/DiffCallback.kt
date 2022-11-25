package com.allan.androidlearning.rcv

import androidx.recyclerview.widget.DiffUtil

abstract class DiffCallback<T>(var olds:List<T>?, var news:List<T>?) : DiffUtil.Callback() {
    final override fun getNewListSize(): Int {
        return news?.size ?: 0
    }

    final override fun getOldListSize(): Int {
        return olds?.size ?: 0
    }

    final override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldData = olds?.get(oldItemPosition)
        val newData = news?.get(newItemPosition)
        if (oldData == null && newData == null) {
            return true
        }
        if (oldData == null || newData == null) {
            return false
        }
        return compareItem(oldData, newData)
    }

    final override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldData = olds?.get(oldItemPosition)
        val newData = news?.get(newItemPosition)
        if (oldData == null && newData == null) {
            return true
        }
        if (oldData == null || newData == null) {
            return false
        }
        return compareContent(oldData, newData)
    }

    abstract fun compareItem(a:T, b:T):Boolean
    abstract fun compareContent(a:T, b:T):Boolean
}