package com.allan.nested.recyclerview.page


import android.util.SparseArray

/**
 * 用于自动加载的分页实现，liveData监控的数据源
 */
open class LoadPage<DATA:Any> {
    //记录每一页的数据,总是按照key的大小排序，key为当前页数
    private val _pageMap = SparseArray<List<DATA>>(4)
    private var _pageIndex = 1
    private var _isOver = true

    val currentPageIndex get() = _pageIndex
    val nextPageIndex get() = _pageIndex + 1
    val isOver get() = _isOver
    val isFirst get() = _pageIndex == 1

    fun getPageByIndex(pageIndex:Int):List<DATA>? {
        return _pageMap.get(pageIndex)
    }

    fun getAllPages(pageIndex:Int):List<DATA> {
        var i = 1
        val num = _pageIndex
        val ret = ArrayList<DATA>()
        while (i <= num) {
            getPageByIndex(i)?.let{ ret.addAll(it) }
            i++
        }
        return ret
    }

    /**
     * 最后一页不满。那么，就证明结束了。有的接口，不告诉我们是否是最后一页了。
     * 所以，通过isOver由外部控制
     */
    fun appendPage(list:List<DATA>, isOver:Boolean) {
        _pageIndex += 1 //注意先+后存。
        _pageMap.put(_pageIndex, list)
        _isOver = isOver
    }

    /**
     * 比如前面刚好是整数page，每页full的情况，可能产生再次请求page为空。直接标记即可。
     */
    fun appendNullMaskEnd() {
        _isOver = true
    }

    /**
     * 当第一次请求为空；则调用这个。
     */
    fun initWithEmpty() {
        _pageIndex = 1
        _pageMap.clear()
        _isOver = true
    }

    /**
     * 当第一次请求不为空；则调用这个。
     */
    fun initPage(list:List<DATA>, isOver:Boolean) {
        _pageMap.clear()
        _pageIndex = 1
        _pageMap.put(_pageIndex, list)
        _isOver = isOver
    }
}