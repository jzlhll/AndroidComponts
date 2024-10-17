package com.allan.nested.recyclerview

import android.util.Log
import androidx.annotation.CallSuper
import androidx.recyclerview.widget.DiffUtil
import com.allan.nested.recyclerview.page.PullRefreshStatus
import com.allan.nested.recyclerview.viewholder.BindViewHolder

/**
 * author: allan
 * Time: 2022/11/22
 * Desc: 基于BindRcvAdapter：简单的封装，提供diff算法；和一些增删改动的动作；提供基础list。进行二次扩展。
 *
 * 这是一个<多页自动加载框架Adapter，当到了底部会自动加载。
 * 设置 [pullRefreshAction] 来支持下拉刷新。
 * 设置 [loadMoreAction] 来支持自动加载下一页loadMore逻辑。
 *
 * 如果支持loadMore则，调用[initDatas]和[appendDatas]
 * 如果不支持loadMore则，调用[submitList]或者[submitListAsync]
 *
 * later：
 * 其实通过代理模式来包装BindRcvAdapter更合适，可以屏蔽ISubmit的使用，但是不便于外部理解。暂时通过throw Exception。
 */

abstract class AutoLoadMoreBindRcvAdapter<DATA:Any, VH: BindViewHolder<DATA, *>> :
        BaseAdapter<DATA, VH>(), ILoadMoreAdapter<DATA> {
    /**
     * 如果支持自动触底loadMore下一页，则需要设置这个参数。
     * 它是当onBindViewHolder最后一个数据的时候自动触发。
     * 默认情况return null则不做loadMore。
     */
    var loadMoreAction:(()->Unit)? = null

    //////////////////////////
    ///
    ////////////////////////
    fun supportLoadMore() = loadMoreAction != null

    private fun onLoadMoreInner() {
        status = PullRefreshStatus.LoadingMore
        loadMoreAction?.invoke()
    }

    fun markAsRefreshing() {
        status = PullRefreshStatus.Refreshing
    }

    private var status: PullRefreshStatus = PullRefreshStatus.Refreshing //默认加载
    fun getCurrentStatus(): PullRefreshStatus = status

    internal var hasMore = false

    fun setNoMore() {
        hasMore = false
    }

    /**
     * 加载更多数据
     */
    override fun appendDatas(appendList: List<DATA>?, hasMore: Boolean) {
        if (!supportLoadMore()) {
            Log.e("allan", "You do not supportLoadMore!")
            throw java.lang.IllegalStateException("You do not supportLoadMore!")
        }
        this.hasMore = hasMore

        appendDatasOnly(appendList)

        status = PullRefreshStatus.Normal
    }

    protected open fun appendDatasOnly(appendList: List<DATA>?) {
        if (!appendList.isNullOrEmpty()) {
            val realDatas = mutableListOf<DATA>()
            realDatas.addAll(appendList)
            addItems(realDatas)
        }
    }

    /**
     * 如果是占位图显示；则需要调用initWithPlacesHolder。替换的时候，不能做差异化更新。
     */
    override fun initDatas(datas: List<DATA>?, hasMore: Boolean, isTraditionalUpdate: Boolean) {
        this.hasMore = hasMore

        val oldDataSize = this.datas.size
        val newDataSize = datas?.size ?: 0

        initDatasOnly(datas, isTraditionalUpdate)

        status = PullRefreshStatus.Normal

        onDataChanged(DataChangeExtraInfoInit(oldDataSize, newDataSize))
    }

    internal open fun initDatasOnly(datas: List<DATA>?, isTraditionalUpdate: Boolean) {
        if (datas.isNullOrEmpty()) {
            updateDataList(null, false, isTraditionalUpdate)
        } else {
            val fixDatas = if (datas == this.datas) { //防止跟本地list相同
                mutableListOf<DATA>().also { it.addAll(datas) }
            } else {
                datas
            }
            updateDataList(fixDatas, false, isTraditionalUpdate)
        }
    }

    /**
     * 主线程刷新 如果是占位图显示；则需要调用initWithPlacesHolder。替换的时候，不能做差异化更新。
     */
    internal fun updateDataList(
        newList: List<DATA>?,
        isReplaceData: Boolean,
        isTraditionalForce:Boolean
    ) {
        if (!isSupportDiffer() || isPlacesHolder || isTraditionalForce) {
            isPlacesHolder = false
            submitTraditional(newList, false)
        } else {
            getDiffResult(newList, isReplaceData)?.dispatchUpdatesTo(this)
        }
    }

    @CallSuper
    final override fun onBindViewHolder(holder: VH, position: Int) {
        if (supportLoadMore() && hasMore && position == itemCount - 1) {
            onLoadMoreInner()
        }
        holder.bindData(datas[position])
    }

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

        val differ = createDiffer(datas, newList)
            ?: throw RuntimeException("BindRcvAdapter: cannot call summitList without implement createDiffer()")

        val result = DiffUtil.calculateDiff(differ, true)
        //完事后，再更改本地list
        if (isReplaceDatas && newList is MutableList<DATA>) {
            datas = newList
        } else {
            datas.clear()
            datas.addAll(newList)
        }
        return result
    }

}