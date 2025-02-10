package com.au.module_okhttp.beans

import androidx.annotation.Keep
import com.au.module_okhttp.beans.base.BaseBean

/**
 * data是json对象
*/
@Keep
class ResultBean<T>(code: String, msg: String?, val data:T? = null) : BaseBean(code, msg)

/**
 * data是json数组
 */
@Keep
class ResultBeanList<T>(code: String, msg: String?, val data: List<T>? = null) : BaseBean(code, msg)

/**
 * 分页数据的每一页数据结构
 * @param currentPage 当前页码
 * @param pages 总页数
 * @param size 每页大小
 * @param total 总数 = pages * size
 * @param data 当前页下的数据list
 */
@Keep
data class Page<T>(
    val currentPage: Int = 0,
    val pages: Int = 0,
    val size: Int = 0,
    val total: Int = 0,
    val records: List<T>? = null
)

/**
 * data是分页
 */
@Keep
class ResultBeanPage<T>(code: String, msg: String?, val data: Page<T>? = null) : BaseBean(code, msg)