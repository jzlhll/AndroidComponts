package com.au.logsystem.oncelog

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.au.logsystem.R
import com.au.module_android.Globals
import com.au.module_android.utils.NoWayException
import com.au.module_android.utils.unsafeLazy
import com.au.module_nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.au.module_nested.recyclerview.DiffCallback
import kotlin.math.min

class LogViewAdapter(private val mRcv: RecyclerView) : AutoLoadMoreBindRcvAdapter<LogViewNormalBean, AbsLogViewBinder<*>>() {
    private var mLoadedToRcvIndex = 0

    private val onceLoadPage = 100

    private var beans:List<LogViewNormalBean>? = null

    val errorColor by unsafeLazy {
        Globals.getColor(R.color.color_log_error)
    }
    val warnColor by unsafeLazy {
        Globals.getColor(R.color.color_log_warn)
    }
    val debugColor by unsafeLazy {
        Globals.getColor(com.au.module_androidcolor.R.color.color_text_normal)
    }

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AbsLogViewBinder<*> {
        return when (viewType) {
            AbsLogViewBinder.VIEW_TYPE_WRAP -> LogViewWrapBinder(create(parent))
            AbsLogViewBinder.VIEW_TYPE_NO_WRAP -> LogViewNoWrapBinder(create(parent))
            else -> throw NoWayException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(datas[position].showBits.isWrap) AbsLogViewBinder.VIEW_TYPE_WRAP else AbsLogViewBinder.VIEW_TYPE_NO_WRAP
    }
}