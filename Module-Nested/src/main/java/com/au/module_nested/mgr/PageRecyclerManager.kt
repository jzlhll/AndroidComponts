package com.au.module_nested.mgr

import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.au.module_nested.layout.NestedLayoutRefresher
import com.au.module_nested.layout.NestedRecyclerViewLayout
import com.au.module_nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.au.module_nested.recyclerview.DataChangeExtraInfoInit
import com.au.module_nested.recyclerview.DataExtraInfo
import com.au.module_nested.recyclerview.IOnChangeListener
import com.au.module_nested.recyclerview.page.AbstractPageViewModel
/**
 * 用于联动下拉刷新框架。
 *
 * recyclerView+支持下拉位移，头部刷新（转圈圈）+loadMore（自定义loading itemHolder类型）的fragment基础类。
 * 然后提供[onRunning], [onError], [onSuccess] 注册监听结果。
 *
 * 这样做的好处在于，脱离了Fragment来继承的方式，本类专注于处理PullRefreshAndAutoLoadMoreView的显示和分页加载的逻辑。
 * 这样Fragment可以自由追加额外控件。而不受限于具体某个xml。
 *
 */
open class PageRecyclerManager<Bean:Any>(
    layout: NestedRecyclerViewLayout,
    viewModel: AbstractPageViewModel<Bean>,
    adapter: AutoLoadMoreBindRcvAdapter<Bean, *>,
    supportPullRefresh: Boolean, //不做default值，给调用者提醒
    supportLoadMore: Boolean = true,
) : PageRecyclerManager2<Bean>(layout.recyclerView, layout.refresher, viewModel, adapter, supportPullRefresh, supportLoadMore)

open class PageRecyclerManager2<Bean:Any>(
    rcv: RecyclerView,
    refresher: NestedLayoutRefresher,
    viewModel: AbstractPageViewModel<Bean>,
    adapter: AutoLoadMoreBindRcvAdapter<Bean, *>,
    supportPullRefresh: Boolean, //不做default值，给调用者提醒
    supportLoadMore: Boolean = true,
) : PageRecyclerOnlyManager<Bean>(rcv, viewModel, adapter, supportLoadMore) {
    init {
        if (supportPullRefresh) {
            this.adapter.addDataChanged(object :IOnChangeListener {
                override fun onChange(info: DataExtraInfo) {
                    if (info is DataChangeExtraInfoInit) {
                        Log.d("allan", "onInitDatasFinished")
                        refresher.refreshCompleted()
                    }
                }
            })

            refresher.setOnRefreshAction {
                viewModel.loadPageData(true)
            }
        }
    }
}