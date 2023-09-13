package com.allan.nested.recyclerview.page

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.module.android.BuildConfig
import com.au.module_android.simplelivedata.SafeLiveData
import com.au.module_android.simplelivedata.Status
import com.au.module_android.simplelivedata.StatusLiveData
import com.au.module_android.toast.toastOnTop
import kotlinx.coroutines.launch

/**
 * 抽象支持PageE的方式，appendDatas的数据源请求父类。
 */
abstract class AbstractPageViewModel<E:Any> : ViewModel() {
    enum class LoadStatus {
        Emitted,
        NoMore,
        IsLoading,
    }

    val pageData by lazy (LazyThreadSafetyMode.NONE) { StatusLiveData<LoadPage<E>>() }

    /**
     * 请求的总共size。只会在realLoadData的第一页成功结果后，回调
     */
    val totalSizeData by lazy (LazyThreadSafetyMode.NONE) { SafeLiveData<Int>() }

    var pageSize = 20

    /**
     * 默认的第一页是从哪来开始。默认是1。可以继承修改。
     */
    var firstPageIndex = 1

    /**
     * 真实进行请求数据的实现逻辑。
     * 已经进行了异常捕获
     */
    abstract suspend fun realLoadData(currentPage: Int, pageSize: Int): ApiPageBean<E>
    /**
     * 真实进行请求数据，如果想做异常处理，可以进行复写。
     */
    open fun realLoadDataError(message:String? = null) {
        toastOnTop(message)
    }

    /**
     * 当前页是否已经触底了。
     * 默认框架：我们并不知道是否已经触底, 只能根据size判断。
     *
     */
    private fun isCurrentPageLastPage(loadedCurrentPage:ApiPageBean<E>) : Boolean {
        if(loadedCurrentPage.currentPage >= loadedCurrentPage.totalPage) return true
        val data = loadedCurrentPage.data ?: return true
        return data.size < pageSize
    }

    /**
     * 调用加载数据。true则变成init，即从page1开始加载；false则appendData
     */
    open fun loadPageData(initOrAppend:Boolean) : LoadStatus {
        if(BuildConfig.DEBUG) Log.d("allan", "load PageData initOrAppend: $initOrAppend")

        val pageDataData = pageData.data
        if (pageDataData != null && pageData.status == Status.RUNNING) {
            return LoadStatus.IsLoading
        }

        if (!initOrAppend && pageDataData != null && pageDataData.isOver) { //append的话，判断是否已经结束。
            return LoadStatus.NoMore
        }

        if(BuildConfig.DEBUG) Log.d("allan", "loadPageData running...${pageData.data}")
        pageData.running()
        val currentPage = if(initOrAppend) firstPageIndex else (pageData.data?.nextPageIndex ?: firstPageIndex)
        viewModelScope.launch {
            val data = try {
                if(BuildConfig.DEBUG) Log.d("allan", "real LoadData----$currentPage")
                realLoadData(currentPage, pageSize) //返回获取到的数据
            } catch (e: Throwable) {
                e.printStackTrace()
                if(BuildConfig.DEBUG) Log.d("allan", "real LoadData error----$currentPage")
                realLoadDataError(e.message)
                null //出错后，返回null
            }

            val myData = pageData.data ?: LoadPage()
            //todo 可以根据http结果data的page信息修改如下的isOver逻辑
            val list = data?.data
            if (initOrAppend) {
                if (list == null || list.size == 0) {
                    if(BuildConfig.DEBUG) Log.d("allan", "init with empty")
                    myData.initWithEmpty()
                    totalSizeData.safeSetValue(0)
                } else {
                    if(BuildConfig.DEBUG) Log.d("allan", "init with page")
                    myData.initPage(list, isCurrentPageLastPage(data))
                    totalSizeData.safeSetValue(data.totalPage)
                }
            } else {
                if (list == null || list.size == 0) {
                    if(BuildConfig.DEBUG) Log.d("allan", "append with end")
                    myData.appendNullMaskEnd()
                } else {
                    if(BuildConfig.DEBUG) Log.d("allan", "append with page")
                    myData.appendPage(list, isCurrentPageLastPage(data))
                }
            }

            if(BuildConfig.DEBUG) Log.d("allan", "realLoadData success!")
            pageData.success(myData)
        }

        if(BuildConfig.DEBUG) Log.d("allan", "start search load page data emitted.")
        return LoadStatus.Emitted
    }
}