package com.au.module_android.okhttp.beans

import androidx.annotation.Keep

/**
 * 分页数据的每一页数据结构
 * @param currentPage 当前页码
 * @param pages 总页数
 * @param size 每页大小
 * @param total 总数 = pages * size
 * @param data 当前页下的数据list
 */
@Keep
data class PageItem<T>(
    val currentPage: Int = 0,
    val pages: Int = 0,
    val size: Int = 0,
    val total: Int = 0,
    val data: List<T>? = null
)
/**
 * data是分页
 *
 * */
@Keep
class ApiPageBean<T>(code: String, msg: String?, val data: PageItem<T>? = null) : BaseBean(code, msg)