package com.allan.androidlearning.rcv.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.allan.androidlearning.rcv.DiffCallback
import com.allan.androidlearning.rcv.ISubmit
import com.allan.androidlearning.rcv.viewholder.BindingViewHolder
import com.allan.androidlearning.utils.withIoThread
import com.allan.androidlearning.utils.withMainThread

/**
 * Desc: 简单的封装，提供diff算法；和一些增删改动的动作；提供基础list。
 */
abstract class BindingRcvAdapter<DATA:Any, VH: BindingViewHolder<DATA, *>> : RecyclerView.Adapter<VH>(), ISubmit<DATA> {
    var datas = mutableListOf<DATA>()

    /**
     * 当需要进行局部化差异更新的时候，会创建differ。
     */
    protected open fun createDiffer(a:List<DATA>?, b:List<DATA>?): DiffCallback<DATA>? {
        return null
    }

    /**
     * 是否支持差异更新。如果支持修改为true；并实现createDiffer
     */
    protected abstract fun isSupportDiffer():Boolean

    override fun getItemCount(): Int {
        return datas.count()
    }

    private fun getDiffResult(
        newList: List<DATA>?,
        isReplaceDatas: Boolean,
    ): DiffUtil.DiffResult? {
        if (newList == null) {
            val count = itemCount
            datas.clear()
            notifyItemRangeRemoved(0, count)
            return null
        }
        val oldData = datas.toMutableList()
        if (isReplaceDatas && newList is MutableList<DATA>) {
            onCurrentListChanged(datas, newList)
            datas = newList
        } else {
            datas.clear()
            datas.addAll(newList)
        }
        val differ = createDiffer(oldData, datas)
            ?: throw RuntimeException("BindRcvAdapter: cannot call summitList without implement createDiffer()")

        return DiffUtil.calculateDiff(differ, true)
    }

    private fun submitTraditional(newList: List<DATA>?, isReplaceDatas: Boolean) {
        if (isReplaceDatas && newList is MutableList<DATA>) {
            onCurrentListChanged(datas, newList)
            datas = newList
        } else {
            datas.clear()
            if (newList != null) {
                datas.addAll(newList)
            }
        }
        notifyDataSetChanged()
    }

    /**
     * 主线程刷新
     */
    override fun submitList(
        newList: List<DATA>?,
        isReplaceDatas: Boolean,
    ) {
        if (!isSupportDiffer()) {
            submitTraditional(newList, isReplaceDatas)
        } else {
            getDiffResult(newList, isReplaceDatas)?.dispatchUpdatesTo(this)
        }
    }

    /**
     * 异步刷新
     */
    override suspend fun submitListAsync(
        newList: List<DATA>?,
        isReplaceDatas: Boolean,
    ) {
        if (!isSupportDiffer()) {
            withMainThread {
                submitTraditional(newList, isReplaceDatas)
            }
        } else {
            val result = withIoThread {
                getDiffResult(newList, isReplaceDatas)
            } ?: return
            withMainThread {
                result.dispatchUpdatesTo(this)
            }
        }
    }

    open fun onCurrentListChanged(previousList: List<DATA>, currentList: List<DATA>) {}

    /**
     * 移除item
     */
    fun removeItem(position: Int, notify: Boolean = true) {
        datas.removeAt(position)
        if (notify) {
            notifyItemRemoved(position)
        }
    }

    /**
     * 移除item
     */
    fun removeItem(startPosition: Int, count: Int, notify: Boolean = true) {
        repeat(count) {
            datas.removeAt(startPosition + (count - it) - 1)
        }
        if (notify) {
            notifyItemRangeRemoved(startPosition, count)
        }
    }

    /**
     * 移除item
     */
    fun removeItem(data: DATA, notify: Boolean = true) {
        val index = datas.indexOf(data)
        if (index >= 0) {
            removeItem(index, notify)
        }
    }

    /**
     * 移除item
     */
    fun removeItems(data: List<DATA>?, notify: Boolean = true) {
        data?.forEach {
            removeItem(it, notify)
        }
    }

    /**
     * 更新item
     */
    fun updateItem(index: Int, data: DATA, payload: Any? = null, notify: Boolean = true) {
        datas[index] = data
        if (notify) {
            notifyItemChanged(index, payload)
        }
    }

    /**
     * 添加item
     */
    fun addItem(data: DATA, index: Int? = null, notify: Boolean = true) {
        val insertIndex = index ?: datas.count()
        datas.add(insertIndex, data)
        if (notify) {
            notifyItemInserted(insertIndex)
        }
    }

    /**
     * 添加item
     */
    fun addItems(data: List<DATA>?, index: Int? = null, notify: Boolean = true) {
        if (data.isNullOrEmpty()) {
            return
        }
        val insertIndex = index ?: datas.count()
        datas.addAll(insertIndex, data)
        if (notify) {
            notifyItemRangeInserted(insertIndex, data.count())
        }
    }

    /**
     * RecyclerView的viewHolder创建，必须传入parent
     */
    @Throws(Exception::class)
    protected fun <T : ViewBinding?> create(
        clazz: Class<T>,
        inflater: LayoutInflater?,
        parent: ViewGroup?
    ): T {
        val o = clazz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        ).invoke(null, inflater, parent, false)
        return o as T
    }
}