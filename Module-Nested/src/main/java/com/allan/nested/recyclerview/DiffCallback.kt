package com.allan.nested.recyclerview

import androidx.recyclerview.widget.DiffUtil

/**
 * author: allan
 * Time: 2022/11/23
 * Desc: 框架设计，由于Adapter设计，list中不存在多种Bean。所以compareItem已经处理完成。
 */
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
        return compareContent(oldData, newData)
    }

    final override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldData = olds?.get(oldItemPosition)
        val newData = news?.get(newItemPosition)
        if (oldData == null && newData == null) { //都为空true
            return true
        }

        if (oldData == null || newData == null) { //其中之一不为空则false
            return false
        }

        return oldData.javaClass == newData.javaClass //根据class类型变化也是一个比较好的处理来判别类型有差别。
    }

    /**
     * 子类实现：用于DiffUtil.Callback计算areContentsTheSame，已经确定，同时不为null。
     * 我们需要比较内容是否有变化。
     * 这里有争议的点是，a == b是否return true，场景是前后adapter提交的list，某些item bean是没有变地址的（直接修改了item对象）。
     * 所以，如果你的提交list很确定，每次来的item list都是新创建出来的则可以使用 a == b来加快比较。
     * 而如果是有后续点击，切换等操作，可能导致源数据的变化而更新recyclerView，则不应该判断a == b.
     */
    abstract fun compareContent(a:T, b:T):Boolean
}