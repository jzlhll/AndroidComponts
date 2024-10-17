package com.allan.nested.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.allan.nested.recyclerview.viewholder.BindViewHolder
import com.au.module_android.utils.unsafeLazy

/**
 * @author au
 * Date: 2023/2/20
 * Description ("这是一个非常基础的类。请勿直接使用。请使用BindRcvAdapter或者AutoLoadMoreXXX")
 *
 */
interface DataExtraInfo
open class DataChangeExtraInfo(val oldDataSize:Int, val newDataSize:Int): DataExtraInfo
class DataUpdateExtraInfo(val updateIndex:Int): DataExtraInfo
class DataChangeExtraInfoInit(oldDataSize:Int, newDataSize:Int) : DataChangeExtraInfo(oldDataSize, newDataSize)
class DataChangeExtraInfoAppend(oldDataSize:Int, newDataSize:Int) : DataChangeExtraInfo(oldDataSize, newDataSize)

interface IOnChangeListener{
    fun onChange(info: DataExtraInfo)
}

abstract class BaseAdapter<DATA:Any, VH: BindViewHolder<DATA, *>> : RecyclerView.Adapter<VH>() {

    var datas = mutableListOf<DATA>()
        internal set

    private val onDataChangedList:ArrayList<IOnChangeListener> by unsafeLazy { ArrayList() }

    fun addDataChanged(listener: IOnChangeListener) {
        if(!onDataChangedList.contains(listener)) onDataChangedList.add(listener)
    }

    fun removeDataChanged(listener: IOnChangeListener) {
        onDataChangedList.remove(listener)
    }
    
    protected fun onDataChanged(info: DataExtraInfo) {
        for (listener in onDataChangedList) {
            listener.onChange(info)
        }
    }

    var isPlacesHolder = false //如果是搞的占位图显示；则需要调用initWithPlacesHolder。然后替换的时候，不能做差异化更新。
        internal set

    override fun getItemCount(): Int {
        return datas.count()
    }

    /**
     * 移除item
     */
    fun removeItem(position: Int, notify: Boolean = true, shouldOnDatasChange:Boolean=true) {
        val oldDataSize = datas.size
        val newDataSize = if(oldDataSize > 0) oldDataSize - 1 else 0
        datas.removeAt(position)
        if (notify) {
            notifyItemRemoved(position)
        }
        if (shouldOnDatasChange) {
            onDataChanged(DataChangeExtraInfo(oldDataSize, newDataSize))
        }
    }

    /**
     * 移除item
     */
    fun removeItem(data: DATA, notify: Boolean = true, shouldOnDatasChange:Boolean=true) {
        val index = datas.indexOf(data)
        if (index >= 0) {
            removeItem(index, notify, shouldOnDatasChange)
        }
    }

    /**
     * 移除item
     */
    fun removeItems(startPosition: Int, count: Int, notify: Boolean = true) {
        val oldDataSize = datas.size
        val newDataSize = oldDataSize - count
        repeat(count) {
            datas.removeAt(startPosition + (count - it) - 1)
        }
        if (notify) {
            notifyItemRangeRemoved(startPosition, count)
        }
        onDataChanged(DataChangeExtraInfo(oldDataSize, newDataSize))
    }

    /**
     * 移除item
     */
    fun removeItems(data: List<DATA>?, notify: Boolean = true) {
        val oldDataSize = datas.size
        val newDataSize = oldDataSize - (data?.size ?: 0)

        data?.forEach {
            removeItem(it, notify, false)
        }

        onDataChanged(DataChangeExtraInfo(oldDataSize, newDataSize))
    }

    /**
     * 更新item
     */
    fun updateItem(index: Int, data: DATA, payload: Any? = null, notify: Boolean = true) {
        datas[index] = data
        if (notify) {
            notifyItemChanged(index, payload)
        }

        onDataChanged(DataUpdateExtraInfo(index))
    }

    /**
     * 添加item
     */
    fun addItem(data: DATA, index: Int? = null, notify: Boolean = true, shouldOnDatasChange: Boolean=true) {
        val insertIndex = index ?: datas.count()
        val oldDataSize = datas.size
        val newDataSize = oldDataSize + 1
        datas.add(insertIndex, data)
        if (notify) {
            notifyItemInserted(insertIndex)
        }

        if (shouldOnDatasChange) {
            onDataChanged(DataChangeExtraInfoAppend(oldDataSize, newDataSize))
        }
    }

    /**
     * 添加item
     */
    fun addItems(data: List<DATA>?, index: Int? = null, notify: Boolean = true) {
        if (data.isNullOrEmpty()) {
            return
        }

        val oldDataSize = datas.size
        val newDataSize = oldDataSize + data.size

        val insertIndex = index ?: datas.count()
        datas.addAll(insertIndex, data)
        if (notify) {
            notifyItemRangeInserted(insertIndex, data.count())
        }

        onDataChanged(DataChangeExtraInfoAppend(oldDataSize, newDataSize))
    }

    @CallSuper
    final override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        //查看父类也只是透给onBindViewHolder ，这里屏蔽掉，去处理给payloads
        //super.onBindViewHolder(holder, position, payloads)
        if (payloads.isEmpty()) {
            onBindViewHolder(holder, position)
        } else {
            holder.payloadsRefresh(datas[position], payloads)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bindData(datas[position])
    }

    internal fun submitTraditional(newList: List<DATA>?, shouldOnDatasChange: Boolean = true) {
        val oldDataSize = datas.size
        val newDataSize = newList?.size ?: 0
        if (newList.isNullOrEmpty()) {
            datas = mutableListOf()
        } else {
            datas = mutableListOf<DATA>().also { it.addAll(newList) }
        }

        notifyDataSetChanged()

        if (shouldOnDatasChange) {
            onDataChanged(DataChangeExtraInfoInit(oldDataSize, newDataSize))
        }
    }

    /**
     *  如果是占位图显示；则需要调用initWithPlacesHolder。替换的时候，不能做差异化更新。
     */
    fun initWithPlacesHolder(newList: List<DATA>?) {
        isPlacesHolder = true
        submitTraditional(newList)
    }

    /**
     * 创建ViewBinding，用于viewHolder的生成。
     */
    inline fun <reified VB : ViewBinding> create(parent: ViewGroup) : VB {
        val o = VB::class.java.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        ).invoke(null, LayoutInflater.from(parent.context), parent, false)
        return o as VB
    }

    fun <VB:ViewBinding> create(clazz:Class<out ViewBinding>, parent: ViewGroup):VB {
        val o = clazz.getMethod(
            "inflate",
            LayoutInflater::class.java,
            ViewGroup::class.java,
            Boolean::class.javaPrimitiveType
        ).invoke(null, LayoutInflater.from(parent.context), parent, false)
        return o as VB
    }
}