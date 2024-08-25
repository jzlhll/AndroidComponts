package com.allan.nested.mgr

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.allan.nested.layout.NestedRecyclerViewLayout
import com.allan.nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.allan.nested.recyclerview.DataChangeExtraInfoInit
import com.allan.nested.recyclerview.DataExtraInfo
import com.allan.nested.recyclerview.IOnChangeListener
import com.allan.nested.recyclerview.page.AbstractPageViewModel
import com.au.module_android.utils.logd

/**
 * 用于联动下拉刷新框架。
 *
 * recyclerView+支持下拉位移，头部刷新（转圈圈）+loadMore（自定义loading itemHolder类型）的fragment基础类。
 * 然后提供[onRunning], [onError], [onSuccess] 注册监听结果。
 *
 * 这样做的好处在于，脱离了Fragment来继承的方式，本类专注于处理PullRefreshAndAutoLoadMoreView的显示和分页加载的逻辑。
 * 这样Fragment可以自由追加额外控件。而不受限于具体某个xml。
 *
 * [initGridAdapterAndRcv] 支持
 * secondary二级页[com.ui.module.second.SecondaryFragment]
 * 和搜索结果页[com.ui.module.search.SearchResultFragment]
 * [initLinearAdapterAndRcv] 支持
 *
 */
open class PageRecyclerManager<Bean:Any>(
    private val layout: NestedRecyclerViewLayout,
    viewModel: AbstractPageViewModel<Bean>,
    adapter: AutoLoadMoreBindRcvAdapter<Bean, *>,
    supportPullRefresh: Boolean, //不做default值，给调用者提醒
    supportLoadMore: Boolean = true,
) : PageRecyclerOnlyManager<Bean>(layout.recyclerView, viewModel, adapter, supportLoadMore) {
    private val rcv:RecyclerView = layout.recyclerView

    init {
        rcv.adapter = adapter

        this.adapter.addDataChanged(object : IOnChangeListener {
            override fun onChange(info: DataExtraInfo) {
                if (info is DataChangeExtraInfoInit) {
                    logd { "onInitDatasFinished" }
                    layout.refresher.refreshCompleted()
                }
            }
        })

        if (supportPullRefresh) {
            layout.refresher.setOnRefreshAction {
                viewModel.loadPageData(true)
            }
        }
    }
}