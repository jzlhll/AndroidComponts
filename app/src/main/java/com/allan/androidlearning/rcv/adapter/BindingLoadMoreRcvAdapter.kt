package com.allan.androidlearning.rcv.adapter

import com.allan.androidlearning.rcv.ILoadMore
import com.allan.androidlearning.rcv.ILoadMoreHelper
import com.allan.androidlearning.rcv.ILoadMoreListener
import com.allan.androidlearning.rcv.viewholder.BindingViewHolder

/**
 * Desc: 基于BindingRcvAdapter：简单的封装，提供diff算法；和一些增删改动的动作；提供基础list。进行二次扩展。
 *
 * 基于BindRcvAdapter；给整个recyclerView添加foot，
 * 即最后一个item是loadMore的ViewHolder类型item
 *
 * 注意：调用者必须通过ILoadMore的方式来调用更新数据。
 * 其实通过代理模式，来包装BindRcvAdapter更合适。这样可以屏蔽ISubmit的使用。但是不便于外部理解。暂时如此。
 */
abstract class BindingLoadMoreRcvAdapter<DATA:Any, VH: BindingViewHolder<DATA, *>> :
    BindingRcvAdapter<DATA, VH>(), ILoadMore<DATA> {
    internal val loadMoreHelper: ILoadMoreHelper = LoaderMoreHelper()

    /**
     *滚动到最后，用于分页
     */
    private var onScrollEndListener: Function1<Int, Unit>? = null

    fun isLoadMoreHolder(position: Int): Boolean {
        return if (position in 0 until datas.count()) {
            return isLoadMoreType(datas[position])
        } else {
            false
        }
    }

    fun getCurrentStatus(): Int = loadMoreHelper.getCurrentStatus()

    /**
     * 添加监听
     */
    fun addListener(l: ILoadMoreListener) = loadMoreHelper.addListener(l)

    /**
     * 移除监听
     */
    fun removeListener(l: ILoadMoreListener?) = loadMoreHelper.removeListener(l)

    /**
     * 加载更多数据
     */
    override fun appendDatas(appendList: List<DATA>?, hasMore: Boolean) {
        val last = datas.lastOrNull()

        if (appendList.isNullOrEmpty()) {
            //如果传入的新数据
            if (last != null && isLoadMoreType(last)) {
                removeItem(last)
            }
            loadMoreHelper.updateStatus(refresh = false, hasMore = false)
        } else {
            val oldCount = itemCount
            val realDatas = mutableListOf<DATA>()
            realDatas.addAll(appendList)
            if (hasMore) {
                realDatas.add(createLoadMoreBean())
            }
            if (last != null && isLoadMoreType(last)) {
                removeItem(oldCount - 1, 1)
            }
            addItems(realDatas)
            loadMoreHelper.updateStatus(false, hasMore)
        }
    }

    override fun initDatas(datas: List<DATA>?, hasMore: Boolean) {
        if (datas.isNullOrEmpty()) {
            //注意，我们为了屏蔽外部使用，已经改写；所以这里调用super
            super.submitList(null, false)
            loadMoreHelper.updateStatus(refresh = true, hasMore = false)
        } else {
            val realDatas = mutableListOf<DATA>()
            realDatas.addAll(datas)
            if (hasMore) {
                //如果有数据，而且还能加载更多，则注册监听
                onScrollEndListener = loadMoreHelper::onLoadMore
                realDatas.add(createLoadMoreBean())
            } else {
                onScrollEndListener = null
            }
            //注意，我们为了屏蔽外部使用，已经改写；所以这里调用super
            super.submitList(realDatas, false)
            loadMoreHelper.updateStatus(true, hasMore)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        onScrollEndListener?.let {
            if (position == itemCount - 1) it(position)
        }
    }

    override fun onBindViewHolder(holder: VH, position: Int, payloads: MutableList<Any>) {
        super.onBindViewHolder(holder, position, payloads)
        onScrollEndListener?.let {
            if (position == itemCount - 1) it(position)
        }
    }

    /**
     * 通过这个设定，框架期待loadMoreBean是一个假类型。也使用DATA这个类型的数据。
     */
    abstract fun createLoadMoreBean():DATA
    abstract fun isLoadMoreType(bean:DATA):Boolean

    //later：暂时通过屏蔽父类函数来实现此类动作。对于有加载的类，需要使用initDatas或者appendDatas处理
    override fun submitList(newList: List<DATA>?, isReplaceDatas: Boolean) {
        //log { "cannot directly call submitList in bindRcvLoadMoreAdapter! Instead with initDatas|appendDatas" }
    }

    //later：暂时通过屏蔽父类函数来实现此类动作。对于有加载的类，需要使用initDatas或者appendDatas处理
    override suspend fun submitListAsync(newList: List<DATA>?, isReplaceDatas: Boolean) {
        //log { "cannot directly call submitList in bindRcvLoadMoreAdapter! Instead with initDatas|appendDatas" }
    }
}

//刷新中
const val STATUS_REFRESHING = 1
//刷新结束
const val STATUS_REFRESH_FINISHED = 2
//加载中
const val STATUS_LOAD_MORE_ING = 3
//加载结束
const val STATUS_LOAD_MORE_FINISHED = 4
//所有数据加载完成
const val STATUS_ALL_DATA_FINISHED = 5