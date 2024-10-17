package com.allan.nested.mgr

import android.os.Bundle
import androidx.core.view.updatePadding
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allan.nested.decoration.GridTwoDownItemDecoration
import com.allan.nested.decoration.PaddingItemDecoration
import com.allan.nested.recyclerview.AutoLoadMore2BindRcvAdapter
import com.allan.nested.recyclerview.AutoLoadMoreBindRcvAdapter
import com.allan.nested.recyclerview.page.AbstractPageViewModel
import com.au.module_android.simplelivedata.Status
import com.au.module_android.utils.dp

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
open class PageRecyclerOnlyManager<Bean:Any>(
    private val rcv: RecyclerView,
    val viewModel: AbstractPageViewModel<Bean>,
    val adapter: AutoLoadMoreBindRcvAdapter<Bean, *>,
    private val supportLoadMore: Boolean = true,
) {


    /**
     * 当回调请求数据，成功的时候，
     * "data"  表示有数据；
     * "empty" 表示为空。
     *
     * 你可以额外做一些信息的显示，adapter和recyclerView相关的更新不用管。
     */
    var onSuccess: ((OnSuccessInfo)->Unit)? = null

    /**
     * 你可以额外做一些信息的显示，adapter和recyclerView相关的更新不用管。
     */
    var onRunning: (()->Unit)? = null

    /**
     * 你可以额外做一些信息的显示，adapter和recyclerView相关的更新不用管。
     */
    var onError:(()->Unit)? = null

    /**
     * 你可以额外做一些信息的显示，adapter和recyclerView相关的更新不用管。
     */
    var onFirstInitError:(()->Unit)? = null

    init {
        rcv.adapter = adapter
    }

    /**
     * 移除了设置padding；设置itemDecoration的做法。
     */
    open fun initGridAdapterAndRcv2(savedInstanceState: Bundle?,
                                   viewLifecycleOwner: LifecycleOwner,
    ) {
        rcv.layoutManager = GridLayoutManager(rcv.context, 2).also {
            //让loading变成占据2个。
            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    if (!adapter.supportLoadMore()) return 1

                    return if (adapter is AutoLoadMore2BindRcvAdapter && adapter.isLoadMoreHolder(position)) {
                        2 //如果是
                    } else {
                        1
                    }
                }
            }
        }
        initOthers(viewLifecycleOwner)
    }

    /**
     * 一种设定方式：Grid 2个一竖的显示方式
     */
    private fun initGridAdapterAndRcv(savedInstanceState: Bundle?,
                                      viewLifecycleOwner: LifecycleOwner,
                                      paddingLeft:Int = 7.5f.dp.toInt(), paddingRight:Int = 7.5f.dp.toInt(),
                                      decoration: RecyclerView.ItemDecoration = GridTwoDownItemDecoration(
                                       10f.dp.toInt(),
                                       10f.dp.toInt()
                                   )
    ) {
        rcv.updatePadding(left = paddingLeft, right = paddingRight)
        rcv.layoutManager = GridLayoutManager(rcv.context, 2).also {
            //让loading变成占据2个。
            it.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    if (!adapter.supportLoadMore()) return 1

                    return if (adapter is AutoLoadMore2BindRcvAdapter && adapter.isLoadMoreHolder(position)) {
                        2 //如果是
                    } else {
                        1
                    }
                }
            }
        }
        rcv.addItemDecoration(decoration)

        initOthers(viewLifecycleOwner)
    }

    /**
     * 一种设定方式：Linear 模式
     */
    open fun initLinearAdapterAndRcv2(savedInstanceState: Bundle?,
                                     viewLifecycleOwner: LifecycleOwner,
    ) {
        rcv.layoutManager = LinearLayoutManager(rcv.context)
        initOthers(viewLifecycleOwner)
    }

    protected open fun initOthers(viewLifecycleOwner: LifecycleOwner) {
        rcv.setHasFixedSize(true)
        if (supportLoadMore) {
            adapter.loadMoreAction = {
                viewModel.loadPageData(false)
            }
        }

        rcv.adapter = adapter

        viewModel.pageData.observe(viewLifecycleOwner) { dataWrap->
            when (dataWrap.status) {
                Status.OVER_SUCCESS -> {
                    val data = dataWrap.data
                    if (data == null || (data.isFirst && data.getPageByIndex(1).isNullOrEmpty())) {
                        adapter.initDatas(listOf(), false)
                        onSuccess?.invoke(OnSuccessInfo.Empty)
                    } else {
                        val onSuccessInfo:OnSuccessInfo
                        if (data.isFirst) {
                            val isTraditional = dataWrap.code == AbstractPageViewModel.LoadCode.InitWithOldMulti.code
                            adapter.initDatas(data.getPageByIndex(1), !data.isOver, isTraditional)
                            onSuccessInfo = OnSuccessInfo.HasData
                        } else {
                            adapter.appendDatas(data.getPageByIndex(data.currentPageIndex), !data.isOver)
                            onSuccessInfo = OnSuccessInfo.HasDataAppend
                        }

                        onSuccess?.invoke(onSuccessInfo)
                    }
                }

                Status.PAGE_INIT_ERROR -> {
                    onFirstInitError?.invoke()
                }

                Status.OVER_ERROR -> {
                    onError?.invoke()
                }

                Status.RUNNING -> {
                    onRunning?.invoke()
                }

                else -> {}
            }
        }
    }
}