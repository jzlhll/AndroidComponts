package com.au.logsystem.oncelog

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.logsystem.databinding.HolderLogViewNormalBinding
import com.au.module_android.utils.logdNoFile
import com.au.module_nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.au.module_nested.recyclerview.DiffCallback
import com.au.module_nested.recyclerview.viewholder.BindViewHolder
import kotlin.math.min

class LogViewAdapter(private val mRcv: RecyclerView) : AutoLoadMoreBindRcvAdapter<LogViewNormalBean, LogViewBinder>() {
    private var mLoadedToRcvIndex = 0

    private val onceLoadPage = 100

    private var beans:List<LogViewNormalBean>? = null

    fun loadNext() {
        val lastIndex = mLoadedToRcvIndex
        this.beans ?: return
        val (hasMore, subList) = nextData(lastIndex)

        if (!mRcv.isAttachedToWindow) {
            return
        }
        appendDatas(subList, hasMore)
    }

    fun initBy(beans:List<LogViewNormalBean>) {
        this.beans = beans
        val lastIndex = mLoadedToRcvIndex
        val (hasMore, subList) = nextData(lastIndex)

        if (!mRcv.isAttachedToWindow) {
            return
        }
        initDatas(subList, hasMore)
    }

    private fun nextData(
        lastIndex: Int
    ): Pair<Boolean, List<LogViewNormalBean>> {
        val beans = this.beans!!
        mLoadedToRcvIndex = min(beans.size - 1, mLoadedToRcvIndex + onceLoadPage)
        val hasMore = mLoadedToRcvIndex + 1 < beans.size
        val subList = beans.subList(lastIndex, mLoadedToRcvIndex + 1)
        return Pair(hasMore, subList)
    }

    override fun isSupportDiffer(): Boolean {
        return true
    }

    private class Differ(aList:List<LogViewNormalBean>?, bList:List<LogViewNormalBean>?) : DiffCallback<LogViewNormalBean>(aList, bList) {
        override fun compareContent(a: LogViewNormalBean, b: LogViewNormalBean): Boolean {
            return a.index == b.index
        }
    }

    override fun createDiffer(a: List<LogViewNormalBean>?, b: List<LogViewNormalBean>?): DiffCallback<LogViewNormalBean> {
        return Differ(a, b)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewBinder {
        return LogViewBinder(create(parent))
    }
}

class LogViewBinder(binding: HolderLogViewNormalBinding) : BindViewHolder<LogViewNormalBean, HolderLogViewNormalBinding>(binding) {
    override fun bindData(bean: LogViewNormalBean) {
        super.bindData(bean)
        binding.textView.text = bean.string
    }
}