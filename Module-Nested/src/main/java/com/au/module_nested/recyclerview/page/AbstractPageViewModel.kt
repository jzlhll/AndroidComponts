package com.au.module_nested.recyclerview.page

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.au.module_android.simplelivedata.NoStickLiveData
import com.au.module_android.simplelivedata.Status
import com.au.module_androidui.toast.toastOnTop
import com.au.module_nested.BuildConfig
import kotlinx.coroutines.launch

/**
 * 抽象支持PageBean的方式，appendDatas的数据源请求父类。
 */
abstract class AbstractPageViewModel<Bean:Any> : ViewModel() {
    enum class LoadStatus {
        Emitted,
        NoMore,
        IsLoading,
    }

    enum class LoadCode(val code:Int) {
        /**
         * 初始化数据：可能是覆盖的老的是一页；或者没有。
         */
        Init(1),

        /**
         * 初始化数据：老的数据一定是多页
         */
        InitWithOldMulti(2),

        /**
         * 拼接数据
         */
        Append(3)
    }

    var logTag:String = ""

    val pageData by lazy (LazyThreadSafetyMode.NONE) { PageStatusLiveData<LoadPage<Bean>>() }

    /**
     * 请求的总共size。只会在realLoadData的第一页成功结果后，回调
     */
    val totalSizeData by lazy (LazyThreadSafetyMode.NONE) { NoStickLiveData<Int>() }

    var pageSize = 20

    /**
     * 默认的第一页是从哪来开始。默认是1。可以继承修改。
     */
    var firstPageIndex = 1

    /**
     * 真实进行请求数据的实现逻辑。
     * 已经进行了异常捕获
     */
    abstract suspend fun realLoadData(currentPage: Int, pageSize: Int): ApiPageBean<Bean>
    /**
     * 真实进行请求数据，如果想做异常处理，可以进行复写。
     */
    open fun realLoadDataError(message:String? = null) {
        if (message != null) {
            toastOnTop(message)
        }
    }

    /**
     * 当前页是否已经触底了。
     * 默认框架：我们并不知道是否已经触底, 只能根据size判断。
     *
     */
    private fun isCurrentPageLastPage(loadedCurrentPage:ApiPageBean<Bean>) : Boolean {
        if (loadedCurrentPage.current >= loadedCurrentPage.pages) {
            return true
        }
        val records = loadedCurrentPage.records ?: return true
        return records.size < pageSize
    }

    /**
     * 调用加载数据。true则变成init，即从page1开始加载；false则appendData
     */
    open fun loadPageData(initOrAppend:Boolean) : LoadStatus {
        if(BuildConfig.DEBUG) Log.d("nested", "${logTag}load PageData initOrAppend: $initOrAppend")

        val pageDataData = pageData.data
        if (pageDataData != null && pageData.status == Status.RUNNING) {
            return LoadStatus.IsLoading
        }

        if (!initOrAppend && pageDataData != null && pageDataData.isOver) { //append的话，判断是否已经结束。
            return LoadStatus.NoMore
        }

        if(BuildConfig.DEBUG) Log.d("nested", "${logTag}loadPageData running...${pageData.data}")
        pageData.runningOldData()
        val currentPage = if(initOrAppend) firstPageIndex else (pageData.data?.nextPageIndex ?: firstPageIndex)

        val myData = pageData.data ?: LoadPage()
        viewModelScope.launch {
            try {
                if(BuildConfig.DEBUG) Log.d("nested", "${logTag}real LoadData----$currentPage")
                val data = realLoadData(currentPage, pageSize) //返回获取到的数据
                val records = data.records

                //todo 可以根据http结果data的page信息修改如下的isOver逻辑

                val loadCode:Int
                var needUpdatePageData = true
                if (initOrAppend) {
                    loadCode = if(!myData.isFirst) LoadCode.InitWithOldMulti.code else LoadCode.Init.code

                    if (records == null || records.size == 0) {
                        if(BuildConfig.DEBUG) Log.d("nested", "${logTag}init with empty")
                        myData.initWithEmpty()
                        totalSizeData.setValueSafe(0)
                    } else {
                        if(BuildConfig.DEBUG) Log.d("nested", "${logTag}init with page")
                        myData.initPage(records, isCurrentPageLastPage(data))
                        totalSizeData.setValueSafe(data.pages)
                    }
                } else {
                    loadCode = LoadCode.Append.code

                    if (records == null || records.size == 0) {
                        if(BuildConfig.DEBUG) Log.d("nested", "${logTag}append with end")
                        myData.appendNullMaskEnd()
                    } else {
                        if(BuildConfig.DEBUG) Log.d("nested", "${logTag}append with page")
                        myData.appendPage(records, isCurrentPageLastPage(data))
                    }
                }

                if(BuildConfig.DEBUG) Log.d("nested", "${logTag}realLoadData success!")
                if(needUpdatePageData) pageData.success(myData, code=loadCode)
            } catch (e: Throwable) {
                //e.printStackTrace()
                val errorMsg:String?
                val errorCode:Int?
                errorCode = -1
                errorMsg = e.message
                if(BuildConfig.DEBUG) Log.d("nested", "${logTag}real LoadData error----$currentPage")
                realLoadDataError(e.message)
                if(BuildConfig.DEBUG) Log.d("nested", "${logTag}load error!")
                pageData.error(myData, errorCode, errorMsg)
            }
        }

        if(BuildConfig.DEBUG) Log.d("nested", "${logTag}start search load page data emitted.")
        return LoadStatus.Emitted
    }
}